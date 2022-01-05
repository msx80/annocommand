package org.github.msx80.annocommand;

import static org.junit.jupiter.api.Assertions.*;

import org.github.msx80.annocommand.AuthorizationTests.User;
import org.junit.jupiter.api.Test;

class MismatchTest {
	
	@Cmd public String hello(String user, String what)
	{
		return "hello "+what;
	}
	
	@Test
	void mismatchTest() 
	{
		Exception e = assertThrows(AnnoCommandException.class, () -> Command.of(User.class, this));
		assertTrue(e.getMessage().toLowerCase().contains("incompatible with context object type"));
	}

	@Test
	void noParamTest() {
		Object o = new Object() {
			@Cmd public String noParams()
			{
				return "hello";
			}
		};
		
		Exception e = assertThrows(AnnoCommandException.class, () -> Command.of(User.class, o));
		assertTrue(e.getMessage().toLowerCase().contains("method has zero params"));
	}

}
