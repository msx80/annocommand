package org.github.msx80.annocommand;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MultiObjectTests {
	
	
	@Test
	void multiObjectTest() {
		
		Object o1 = new Object() {
			@Cmd public String hello(String what)
			{
				return "hello "+what;
			}
		};
		
		Object o2 = new Object() {
			@Cmd public String goodbye(String what)
			{
				return "goodbye "+what;
			}
		};
		
		
		
		Command<Void> c = Command.of(o1, o2);
		assertEquals(c.execute("hello world"), "hello world");
		assertEquals(c.execute("goodbye world"), "goodbye world");
		
	}

}
