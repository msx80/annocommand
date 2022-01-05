package org.github.msx80.annocommand;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HelloWorldTest {

	@Cmd public String greet(String what)
	{
		return "hello "+what+"!";
	}

	@Test
	void basicUsage() {
		Command<Void> c = Command.of(this);
		assertEquals(c.execute("greet world"), "hello world!");
		
	}

}
