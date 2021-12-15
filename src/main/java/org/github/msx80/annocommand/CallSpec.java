package org.github.msx80.annocommand;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CallSpec {
	
	public final String cmd;
	public final int numParam;
	protected final Parameter[] params;
	protected final Method method;
	protected final Object objectToCall;
	private boolean ignoreCase;
	
	public CallSpec(Method m, Object objectToCall, int numContextParams, boolean ignoreCase) {
		this.cmd = m.getName();
		this.objectToCall = objectToCall;
		this.numParam = m.getParameterCount() -numContextParams; 
		this.method = m;
		this.ignoreCase = ignoreCase;

		if(numParam<0) throw new AnnoCommandException("Less parameter than the number of context parameter for "+this);
		
		params = new Parameter[numParam];
		for (int i = 0; i < numParam; i++) {
			Parameter p = m.getParameters()[i+numContextParams];
			params[i] = p;
		}
	}

	public boolean match(String[] text)
	{
		if(ignoreCase)
		{
			if(!cmd.equalsIgnoreCase(text[0])) return false;
		}
		else
		{
			if(!cmd.equals(text[0])) return false;
		}
		int numPar = text.length-1 ; // firse one is cmd;
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
