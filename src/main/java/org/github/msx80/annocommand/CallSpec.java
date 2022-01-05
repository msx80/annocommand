package org.github.msx80.annocommand;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CallSpec {
	
	public final String cmd;
	public final int numParam;
	protected final Parameter[] params;
	protected final Method method;
	protected final Object objectToCall;

	protected final Collection<Class<? extends Annotation>> requiredAnnotations;
	private final boolean wantContextObject;

	
	public CallSpec(Method m, Object objectToCall, boolean wantContextObject, Class<?> contextObjectClass) {
		this.wantContextObject = wantContextObject;
		this.cmd = m.getName();
		this.objectToCall = objectToCall;
		this.numParam = m.getParameterCount(); 
		this.method = m;

		if((numParam==0) && wantContextObject) throw new AnnoCommandException("Method has zero params but wantContextObject = true, should have at least the context params. "+m.getName());
		
		params = new Parameter[numParam];
		for (int i = 0; i < numParam; i++) {
			Parameter p = m.getParameters()[i];
			params[i] = p;
		}
		
		if(wantContextObject)
		{
			if(!params[0].getType().isAssignableFrom(contextObjectClass))
			{
				throw new AnnoCommandException("First parameter type ("+params[0].getType().getCanonicalName()+") incompatible with context object type ("+contextObjectClass.getCanonicalName()+") for method "+m.getName());
			}
		}
		
		requiredAnnotations = new ArrayList<>();
		Annotation[] required = method.getAnnotations();
		for (Annotation a : required) {
			if(a.annotationType() != Cmd.class)
			{
				requiredAnnotations.add(a.annotationType());
			}
		}
		
	}

	public boolean match(String[] text, boolean ignoreCase)
	{
		if(ignoreCase)
		{
			if(!cmd.equalsIgnoreCase(text[0])) return false;
		}
		else
		{
			if(!cmd.equals(text[0])) return false;
		}
		int numPar = text.length-1 + (wantContextObject ? 1 : 0); // firse one is cmd;

		if(numPar >= numParam)
		{
			return true;
		}
		
		return false;
		
	}

	public String getCallString()
	{
		String pae = Stream.of(params).map(p -> p.getType().getSimpleName()).collect(Collectors.joining(", "));
		return cmd + " " +pae;
	}

	public int getNumParam() {
		return numParam;
	}
	
	protected String getUniqueKey()
	{
		return cmd+"#"+numParam;
	}

	@Override
	public String toString() {
		return "[cmd=" + cmd + ", numParam=" + numParam + ", objectToCall=" + objectToCall + "]";
	}
	
	
}
