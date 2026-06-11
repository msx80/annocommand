package org.github.msx80.annocommand;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class TokenizerParenTest {

	@Cmd public Integer sum(int a, int b)
	{
		return a+b;
	}

	@Cmd public Integer mult(int a, int b)
	{
		return a*b;
	}
	
	private static String[] tokenize(Void v, String command)
	{
		String cmd = StringUtils.trim( StringUtils.substringBefore(command, "(") );
		String paramsStr = StringUtils.substringBetween(command, "(", ")");
		String[] params = paramsStr.split(",");
		String[] res = new String[params.length+1];
		res[0] = cmd;
		for (int i = 0; i < params.length; i++) {
			res[i+1] = StringUtils.trim(params[i]);
		}
		return res;
	}
	

	@Test
	void basicUsage() {
		Command<Void> c = Command.of(this);
		c.setTokenizer(TokenizerParenTest::tokenize);
		
		assertEquals(c.execute("sum(34,32)"), 66);
		assertEquals(c.execute("mult ( 3 , 9 )"), 27);
	
	}

	
}
