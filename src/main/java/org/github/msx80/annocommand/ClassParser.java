package org.github.msx80.annocommand;

import java.lang.reflect.Parameter;

public interface ClassParser<V, C> 
{
	/**
	 * Instantiate an object of class C based on the text received.
	 * @param param the Parameter that need converting, should you need to check something here
	 * @param parameterText the text to convert to object
	 * @param context the context object, should you need it to convert the text
	 * @return
	 */
	V parseParameterObject(Parameter param, String parameterText, C context);
	
	
	
}
