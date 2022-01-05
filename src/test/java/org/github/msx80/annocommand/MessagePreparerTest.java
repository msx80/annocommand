package org.github.msx80.annocommand;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MessagePreparerTest {

	@Cmd public String hello(String what)
	{
		return "hello "+what;
	}

	@Cmd public String hello(String what, String what2)
	{
		return "hello2 "+what+" "+what2;
	}


	@Test
	void basicUsage() {
		Command<Void> c = Command.of(this);
		c.setTokenizer((v,s)-> s.split(","));
		assertEquals(c.execute("hello,world"), "hello world");
		assertEquals(c.execute("hello,cruel world"), "hello cruel world");
		assertEquals(c.execute("hello,cruel,world"), "hello2 cruel world");
	
	}

}
