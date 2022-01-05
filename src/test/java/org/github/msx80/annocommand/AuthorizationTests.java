package org.github.msx80.annocommand;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;

class AuthorizationTests {
	
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface Admin {

	}
	
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface Modify {

	}
	
	public static class User  {

		public final String username;
		public final Collection<Class<? extends Annotation>> auths;

		@SafeVarargs
		public User(String username, Class<? extends Annotation>... auths) {
			super();
			this.username = username;
			this.auths = Arrays.asList(auths);
		}

		@Override
		public int hashCode() {
			return username.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof User ? this.username.equals(((User)obj).username) : false;
		}

		@Override
		public String toString() {
			return "["+username+"]";
		}

		
	}


	@Cmd @Admin public String drop(User user, String what)
	{
		return "drop "+what;
	}

	@Cmd @Modify public String update(User user, String what)
	{
		return "update "+what;
	}

	@Cmd public String query(User user, String what)
	{
		return "query "+what;
	}

	
	@Test
	void authorization() {
		Command<User> command = Command.of(User.class, this);
		command.enableAuthorizationChecking(u -> u.auths);
		
		User a = new User("alice", Admin.class, Modify.class);
		User b = new User("bob", Modify.class);
		User c = new User("charlie");
		
		assertEquals(command.execute(a, "query table"), "query table");
		assertEquals(command.execute(b, "query table"), "query table");
		assertEquals(command.execute(c, "query table"), "query table");
		
		assertEquals(command.execute(a, "update table"), "update table");
		assertEquals(command.execute(b, "update table"), "update table");
		AnnoCommandException e = assertThrows(AnnoCommandException.class, () -> command.execute(c, "update table"));
		assertEquals(e.getMessage(), "Unauthorized!");

		assertEquals(command.execute(a,"drop table"), "drop table");
		AnnoCommandException e2 = assertThrows(AnnoCommandException.class, () -> command.execute(b, "drop table"));
		assertEquals(e2.getMessage(), "Unauthorized!");
		AnnoCommandException e3 = assertThrows(AnnoCommandException.class, () -> command.execute(c, "drop table"));
		assertEquals(e3.getMessage(), "Unauthorized!");
		
		command.setAuthorizationFallback((user, message) -> "noway "+user.username);
		assertEquals(command.execute(c, "drop table"), "noway charlie");
	}

}
