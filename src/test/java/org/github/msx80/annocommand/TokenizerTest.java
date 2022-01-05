package org.github.msx80.annocommand;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TokenizerTest {

	@Cmd public String hello(String what)
	{
		return "hello "+what;
	}

	@Cmd public String replaced()
	{
		return "replaced";
	}

	private String prepare(Void param, String m)
	{
		m = m.trim();
    	if(!m.startsWith("!"))
		{
			return null;
		}
		if(m.equals("!replaceme")) m = "!replaced";
		// remove leading !
		return m.substring(1);
	}
	    

	@Test
	void basicUsage() {
		Command<Void> c = Command.of(this);
		c.setMessagePreparer(this::prepare);
		assertEquals(c.execute("!hello world"), "hello world");
		assertNull(c.execute("hello world"));
		assertEquals(c.execute("!replaceme"), "replaced");
	
	}

}
