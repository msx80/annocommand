package org.github.msx80.annocommand;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


/**
 * This class maps string command to object methods automatically.
 * For example it can turn "sum 5 4" into a call to a method "sum(int a, int b)"
 *
 */
public class Command {

	private static final Object[] EMPTY = new Object[0];

	
	 private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new HashMap<>();
	 
	 static {
		 PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
		 PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
		 PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
		 PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
		 PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
		 PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
		 PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
		 PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
		 PRIMITIVES_TO_WRAPPERS.put(void.class, Void.class);
	 }
	
	List<CallSpec> calls = new ArrayList<>();

	
	private Map<Class<?>, ClassParser<?>> classParsers = new HashMap<>();
	private BiFunction<String, Object[], String> preparer = (text, context) -> text;
	private BiFunction<String, Object[], String[]> tokenizer = (text, context) -> text.split(" +");
	private int numContextParameters;
	private boolean ignoreCase;

	/**
	 * Create a new Command to parse text commands, that will be sent the first matching method of the provided objects
	 * @param numContextParameters number of extra parameters depending on context, handled outside of the string (like session or user identifiers)
	 * @param objectsWithCommands the actual objects whose methods will be called
	 */
	public Command(int numContextParameters, boolean ignoreCase, Object... objectsWithCommands) {
		
		this.ignoreCase = ignoreCase;
		this.numContextParameters = numContextParameters;
		for (Object object : objectsWithCommands) 
		{
			analyzeObject(object);
		}
		
		checkValidity(calls);
		calls.sort((a,b) -> Integer.compare(b.getNumParam(), a.getNumParam())); // importante per matchare frasi piu' lunghe prima delle piu' corte!

		//default parsers, if you don't like them you can register your own.
		this.registerClassParser(String.class, ( p, text, o) ->  text);
		this.registerClassParser(Boolean.class, ( p, text, o) ->  Boolean.parseBoolean(text));
		this.registerClassParser(Long.class, ( p, text, o) ->  Long.parseLong(text));
		this.registerClassParser(Integer.class, ( p, text, o) ->  Integer.parseInt(text));
		this.registerClassParser(Character.class, ( p, text, o) ->  { if (text.length()>1) throw new AnnoCommandException("Character parameter is longer than 1"); return text.charAt(0); });
		this.registerClassParser(BigInteger.class, ( p, text, o) ->  new BigInteger(text));
		this.registerClassParser(BigDecimal.class, ( p, text, o) ->  new BigDecimal(text));
		this.registerClassParser(String[].class, ( p, text, o) ->  text.split(" +"));
	}

	/**
	 * Register a class parser. When a parameter class is "cls", the system will use the provided ClassParser to reconstruct the object from the string.
	 * Several class parsers for standard types are already registered at startup
	 * @param cls
	 * @param cp
	 */
	public <C> void registerClassParser(Class<C> cls, ClassParser<C> cp)
	{
		classParsers.put(cls, cp);
	}

	private void analyzeObject(Object objectWithCommands) {
		Method[] ms = objectWithCommands.getClass().getMethods();
		for (Method m : ms) {
			if(m.isAnnotationPresent(Cmd.class))
			{
				CallSpec c = new CallSpec(m, objectWithCommands, numContextParameters, ignoreCase);
				calls.add(c);
			}
		}
	}
	
	
	private static void checkValidity(List<CallSpec> calls2) {
		Map<String, CallSpec> keys = new HashMap<>();
		for (CallSpec c : calls2) {
			String k = c.getUniqueKey();
			CallSpec c2 = keys.get(k);
			if(c2 != null)
			{
				// error!
				throw new AnnoCommandException("Ambiguous call between "+c+" and "+c2);
			}
			else
			{
				keys.put(k,  c);
			}
		}
	}


	
	/**
	 * Set a function that can process the string before being sent to execution.
	 * For example you can use it to remove leading slashes, lowercase commands etc.
	 * You can also return null, in this case the message will be discarded.
	 * 
	 * The function will receive the text to prepare and the context parameters, so it
	 * can behave differently based on context.
	 * 
	 * @param preparer A new messagePreparer
	 * @return this
	 * 
	 */
	public Command setMessagePreparer(BiFunction<String, Object[], String> preparer) {
		this.preparer = preparer;
		return this;
	}
	
	
	
	/**
	 * Set a custom tokenizer to the commander. The tokenizer is responsible of
	 * dividing the input string into pieces that will be matched to method parameters.
	 * Default tokenizer divide by spaces ( text.split(" +"); ).
	 * You can provide a custom tokenizer, for example to add quotes-delimited text or
	 * such. Context parameters are passed to customize tokenization per context.
	 * @param tokenizer
	 * @return this
	 */
	public Command setTokenizer(BiFunction<String, Object[], String[]> tokenizer) {
		this.tokenizer = tokenizer;
		return this;
	}

	/**
	 * Execute a command.
	 * @param m The text to parse and execute
	 * @param context context parameters in number equal to the one configured at construction time
	 * @return any returned object from the called method.
	 * @throws NoMatchingMethodException if no method could be found matching the text
	 */
	public Object execute(String m, Object... context) throws NoMatchingMethodException
	{
		if (context == null) context = EMPTY;
		if(context.length != numContextParameters) throw new AnnoCommandException("Context parameters should be "+numContextParameters+", are "+context.length);
		
		String text = preparer.apply(m, context);
		if(text == null) return null;
		String[] tok = tokenizer.apply(text, context);
		
		for (CallSpec callSpec : calls) {
			if(callSpec.match(tok))
			{
				// User u = db.parseUser(m.from(), m.regno());
				return invoke(callSpec, context, tok);
				
			}
		}
		throw new NoMatchingMethodException(tok[0], "No method matching request.");
		
	}
	
	/**
	 * List all available commands
	 * @return a list of all commands with parameters.
	 */
	public List<String> availableCommands()
	{
		return calls
				.stream()
				.map( c -> c.getCallString())
				.sorted()
				.collect(Collectors.toList());
	}
	
	/**
	 * List all available versions of a given command
	 * @param baseCommand the command to list
	 * @return a list of all commands with parameters.
	 */
	public List<String> availableCommands(String baseCommand)
	{
		return calls
				.stream()
				.filter(c -> c.cmd.equals(baseCommand))
				.map( c -> c.getCallString())
				.sorted()
				.collect(Collectors.toList());
	}
	
	
	private Object invoke(CallSpec call, Object[] context, String[] text)
	{
		text = Arrays.copyOfRange(text, 1, text.length);
		if(text.length > call.numParam)
		{
			// there are more tokens than parameters. Join all extra into the last
			text = String.join(" ", text).split(" ",call.numParam);
		}
		
		// now text is exacly the correct number
		Object[] params = new Object[numContextParameters+call.numParam];
		
		// first pass context parameters
		for (int i = 0; i < context.length; i++) {
			params[i] = context[i];
		}
		
		for (int i = 0; i < call.numParam; i++) {
	
			Object o;
			try {
				o = paramToObject(call.params[i], text[i], context);
			} catch (Exception e) {
				throw new AnnoCommandException("Error parsing parameter #"+i+" for "+call+": "+e.getMessage(), e); 
			}

			params[numContextParameters+i] = o;
		}
		
		try {
			try
			{
				return call.method.invoke(call.objectToCall, params);
			}
			catch(InvocationTargetException e)
			{
				throw e.getCause();
			}
		}
		 catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object paramToObject(Parameter param, String valueToParse, Object[] context) {
		Object o;
		Class<?> cls = param.getType();
		
		cls = cls.isPrimitive() ? PRIMITIVES_TO_WRAPPERS.get(cls) : cls; // convert primiteve classes to wrappers
		
		ClassParser parser = classParsers.get(cls);
		
		if(parser == null)
		{
			if(cls.isEnum())
			{
				// if it's enum, let's parse it with standard stuff
				o = Enum.valueOf((Class<? extends Enum>)cls, valueToParse);
			}
			else
			{
				throw new AnnoCommandException("No ClassParser found for class "+cls.getName()+". Register one with registerClassParser().");
			}
		}
		else
		{
			o = parser.parseParameterObject(param, valueToParse, context);
			if(o!=null)
			{
				if(!cls.isInstance(o))
				{
					throw new AnnoCommandException("Expected instance of class "+cls.getName()+", obtained "+o.getClass().getName());
				}
			}
		}
		return o;
	}

	
}
