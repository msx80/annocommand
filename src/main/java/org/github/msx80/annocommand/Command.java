package org.github.msx80.annocommand;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
public class Command<C> {

	
	 private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new HashMap<>();

	private static final Collection<Class<? extends Annotation>> EMPTY_ANNOTATION_LIST = Arrays.asList();
	 
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
	
	private final List<CallSpec> calls = new ArrayList<>();
	private final Class<C> contextObjectClass;
	private final boolean wantsContextObject;
	private final Map<Class<?>, ClassParser<?, C>> classParsers = new HashMap<>();

	private BiFunction<C, String, String> preparer = (context, text) -> text;
	private BiFunction<C, String, String[]> tokenizer = (context, text) -> text.split(" +");
	private AuthorizationLoader<C> authorizationLoader;
	private BiFunction<C, String, Object> authorizationFallback;
	private boolean ignoreCase = false;
	
	/**
	 * Create a new Command to parse text commands, that will be sent the first matching method of the provided objects
	 * A context object is passed along to the methods and must appear as the first parameter.
	 * @param objectsWithCommands the actual objects whose methods (tagged with @Cmd) will be called
	 */
	public static <E> Command<E> of(Class<E> contextObjectClass, Object... objectsWithCommands)
	{
		return new Command<E>(true, contextObjectClass, objectsWithCommands);
	}
	
	/**
	 * Create a new Command to parse text commands, that will be sent the first matching method of the provided objects
	 * @param objectsWithCommands the actual objects whose methods (tagged with @Cmd) will be called
	 */

	public static Command<Void> of(Object... objectsWithCommands)
	{
		return new Command<Void>(false, Void.class, objectsWithCommands);
	}
	
	private Command(boolean wantsContextObject, Class<C> contextObjectClass, Object... objectsWithCommands) {
		

		this.wantsContextObject = wantsContextObject;
		this.contextObjectClass = contextObjectClass;
		
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
	public <V> void registerClassParser(Class<V> cls, ClassParser<V, C> cp)
	{
		classParsers.put(cls, cp);
	}

	private void analyzeObject(Object objectWithCommands) {
		Method[] ms = objectWithCommands.getClass().getMethods();
		for (Method m : ms) {
			if(m.isAnnotationPresent(Cmd.class))
			{

				CallSpec c = new CallSpec(m, objectWithCommands, wantsContextObject, contextObjectClass);
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
	 * You can also return null, in this case the message will be discarded and the execute()
	 * method will return null.
	 * The function will receive the context object along with the text to prepare, so it
	 * can behave differently based on context.
	 * 
	 * @param preparer A new messagePreparer
	 * @return this
	 * 
	 */
	public Command<C> setMessagePreparer(BiFunction<C, String, String> preparer) {
		this.preparer = preparer;
		return this;
	}
	
	
	
	/**
	 * Set a custom tokenizer to the commander. The tokenizer is responsible of
	 * dividing the input string into pieces that will be matched to method parameters.
	 * Default tokenizer divide by spaces ( text.split(" +"); ).
	 * You can provide a custom tokenizer, for example to add quotes-delimited text or
	 * such. Context object is passed to customize tokenization per context.
	 * @param tokenizer
	 * @return this
	 */
	public Command<C> setTokenizer(BiFunction<C, String, String[]> tokenizer) {
		this.tokenizer = tokenizer;
		return this;
	}

	/**
	 * Execute a command without a context object.
	 * @param m The command to execute
	 * @return The return value of whichever method is found and called
	 * @throws NoMatchingMethodException if no method could be found matching the text
	 */
	public Object execute(String m) throws NoMatchingMethodException
	{
		if(wantsContextObject) throw new AnnoCommandException("Calling execute without context object but wantsContextObject = true");
		return execute(null, m);
	}
	/**
	 * Execute a command, passing the context object to the called method (as first parameter)
	 * @param m The text to parse and execute
	 * @param context context object as defined in the constructor
	 * @return any returned object from the called method.
	 * @throws NoMatchingMethodException if no method could be found matching the text
	 */
	public Object execute(C context, String m) throws NoMatchingMethodException
	{
			
		String text = preparer.apply(context, m);
		if(text == null) return null;
		String[] tok = tokenizer.apply(context, text);
		
		for (CallSpec callSpec : calls) {
			if(callSpec.match(tok, ignoreCase))
			{
				if(authorizationLoader != null)
				{
					boolean checkOk = checkAuthorization(callSpec, context);
					if(!checkOk)
					{
						if(authorizationFallback == null)
							throw new AnnoCommandException("Unauthorized!");
						else 
							return authorizationFallback.apply(context, m);
					}
				}
				// User u = db.parseUser(m.from(), m.regno());
				return invoke(callSpec, context, tok);
				
			}
		}
		throw new NoMatchingMethodException(tok[0], "No method matching request.");
		
	}

	private boolean checkAuthorization(CallSpec callSpec, C context) 
	{
		Collection<Class<? extends Annotation>> required = callSpec.requiredAnnotations;

		if(required.isEmpty()) return true; // method doesn't require special authorization, return fast
		
		Collection<Class<? extends Annotation>> availableAnnotation = authorizationLoader.getAuths(context);
		if(availableAnnotation==null) availableAnnotation = EMPTY_ANNOTATION_LIST;
		
		for (Class<? extends Annotation> a : required) {
		
			if(!availableAnnotation.contains(a))
			{
				return false;
			}
	
		}
		return true;
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
	
	
	private Object invoke(CallSpec call, C context, String[] text)
	{
		int realParamCount = call.numParam - (wantsContextObject ? 1:0);
		text = Arrays.copyOfRange(text, 1, text.length);
		if(text.length > ( realParamCount ))
		{
			// there are more tokens than parameters. Join all extra into the last
			text = String.join(" ", text).split(" ",realParamCount);
		}
		// now text is exacly the correct number
		Object[] params = new Object[call.numParam];
		
		int startFrom;
		// first pass context parameters
		if(wantsContextObject)
		{
			params[0] = context;
			startFrom = 1;
		}
		else
		{
			startFrom = 0;
		}
		
		for (int i = startFrom; i < call.numParam; i++) {
	
			Object o;
			try {
				o = paramToObject(call.params[i], text[i-startFrom], context);
			} catch (Exception e) {
				throw new AnnoCommandException("Error parsing parameter #"+i+" for "+call+": "+e.getMessage(), e); 
			}

			params[i] = o;
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
	private Object paramToObject(Parameter param, String valueToParse, C context) {
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

	/**
	 * Enable authorization checking. If enabled, commands can have extra Annotations that represents authorization capabilities, like @Admin or @Modify
	 * To actually execute the method, the context object is queried to check what authorization capabilities are associated and if all of the required ones
	 * are present. The provided AuthorizationLoader tells the system how to get the tokens of a given context.
	 * @param authorizationLoader a loader to be used to get all authorizations associated to a context (for example an user)
	 * @return this object to chain other method calls
	 */
	public Command<C> enableAuthorizationChecking(AuthorizationLoader<C> authorizationLoader) {
		this.authorizationLoader = authorizationLoader;
		return this;
	}

	/**
	 * Normally, if authorization checking is enabled and the user doesn't have the authorization, an exception is thrown. With
	 * this method you can set an alternative behaviour for this case.
	 * @param authorizationFallback the function is called whenever a method cannot be executed for lack of authorizations.
	 * @return this object to chain other method calls
	 */
	public Command<C> setAuthorizationFallback(BiFunction<C, String, Object> authorizationFallback) {
		this.authorizationFallback = authorizationFallback;
		return this;
	}

	public void setCaseInsensitive(boolean caseInsensitive) {
		this.ignoreCase = caseInsensitive;
	}
	
	
}
