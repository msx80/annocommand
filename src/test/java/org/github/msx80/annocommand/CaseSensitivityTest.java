package org.github.msx80.annocommand;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CaseSensitivityTest {

	@Cmd public String greet(String what)
	{
		return "hello "+what+"!";
	}

	@Test
	void caseSensitivityTest() {
		Command<Void> c = Command.of(this);
		c.setCaseInsensitive(true);
		assertEquals(c.execute("greet world"), "hello world!");
		assertEquals(c.execute("GrEeT world"), "hello world!");
		c.setCaseInsensitive(false);
		assertEquals(c.execute("greet world"), "hello world!");
		assertThrows(NoMatchingMethodException.class, () -> c.execute("GrEeT world"));
	}

}
