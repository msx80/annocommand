package org.github.msx80.annocommand;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ParamNumberTests {

	@Cmd public String hello(String what)
	{
		return "hello "+what;
	}

	@Cmd public String aa(String param)
	{
		return "aa1";
	}

	@Cmd public String aa(String param1, String param2)
	{
		return "aa2";
	}

	@Cmd public String noPar()
	{
		return "ok";
	}
	
	@Test
	void basicUsage() {
		Command<Void> c = Command.of(this);
		assertEquals(c.execute("hello world"), "hello world");
		
		// extra parameters are joined
		assertEquals(c.execute("hello cruel world"), "hello cruel world");
		
		assertThrows(NoMatchingMethodException.class, () -> c.execute("hello"));
		
		assertEquals(c.execute("aa x"), "aa1");
		assertEquals(c.execute("aa x y"), "aa2");
		assertEquals(c.execute("aa x y z"), "aa2");
		

		assertEquals(c.execute("noPar"), "ok");
		assertEquals(c.execute("noPar x"), "ok");
	}

}
