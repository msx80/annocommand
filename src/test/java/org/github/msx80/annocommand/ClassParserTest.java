package org.github.msx80.annocommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.github.msx80.annocommand.AuthorizationTests.User;
import org.junit.jupiter.api.Test;
import java.awt.Point;

class ClassParserTest {
	
	@Cmd public String hello(User user)
	{
		return "Hello "+user; 
	}
	
	@Cmd public int mult(Point p)
	{
		return p.x * p.y;
	}
	
	@Test
	void exceptionTest() {
		Command<Void> u = Command.of( this );
		//u.registerClassParser(User.class,  (param, text, context) -> new User(text));
		
		Exception e = assertThrows(AnnoCommandException.class,() -> u.execute("hello john") );

		assertTrue(e.getMessage().toLowerCase().contains("error parsing parameter #"));
		
		assertTrue(e.getMessage().toLowerCase().contains("no classparser found for class"));
	}
	
	@Test
	void okTest() {
		Command<Void> u = Command.of( this );
		u.registerClassParser(User.class,  (param, text, context) -> new User(text));
		
		assertEquals(u.execute("hello john"), "Hello [john]" );
	}
	
	@Test
	void pointTest() {
		Command<Void> u = Command.of( this );
		u.registerClassParser(Point.class,  (param, text, context) -> {
			String[] tok = text.split("-");
			return new Point(Integer.parseInt(tok[0]), Integer.parseInt(tok[1]));
		});
		
		assertEquals(u.execute("mult 4-7"), 28 );
	}

}
