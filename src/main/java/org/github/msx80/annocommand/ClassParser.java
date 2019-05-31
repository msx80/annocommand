package org.github.msx80.annocommand;

import java.lang.reflect.Parameter;

public interface ClassParser<C> 
{
	/**
	 * Instantiate an object of class C based on the text received.
	 * @param param the Parameter that need converting, should you need to check something here
	 * @param parameterText the text to convert to object
	 * @param context the extra objects, should you need them to convert the text
	 * @return
	 */
	C parseParameterObject(Parameter param, String parameterText, Object[] context);
	
	
	
}
