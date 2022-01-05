package org.github.msx80.annocommand;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AmbiguityTests {
	
	
	@Test
	void multiObjectTest() {
		
		Object o1 = new Object() {
			@Cmd public String hello(String what)
			{
				return "hello "+what;
			}
		};
		
		Object o2 = new Object() {
			@Cmd public String hello(String what)
			{
				return "hello "+what;
			}
		};
		
		
		AnnoCommandException e = assertThrows(AnnoCommandException.class, () -> Command.of(o1, o2));
		assertTrue(e.getMessage().toLowerCase().contains("ambiguous call"));
	}

}
