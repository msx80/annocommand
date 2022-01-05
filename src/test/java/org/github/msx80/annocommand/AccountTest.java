package org.github.msx80.annocommand;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.github.msx80.annocommand.AuthorizationTests.User;
import org.junit.jupiter.api.Test;

class AccountTest {
	
	public class Accounts {

		private Map<User, Integer> credit = new HashMap<>();
		
		@Cmd public void increase(User sender, Integer quantity)
		{
			accredit(sender, quantity);
			System.out.println(sender+"> done");
		}

		private void accredit(User account, Integer quantity) {
			Integer c = credit.getOrDefault(account, 0);
			credit.put(account, c+quantity);
		}
		
		@Cmd public void credit(User sender)
		{
			System.out.println(sender+"> credit is: "+credit.getOrDefault(sender, 0));
		}
		
		
		@Cmd public void move(User sender, Integer quantity, User recipient)
		{
			accredit(sender, -quantity);
			accredit(recipient, quantity);
			System.out.println(sender+"> moved "+quantity+" to "+recipient);
		}
		
		
	}
	
	@Test
	void accountTest() {
		User john = new User("john");
		User mike = new User("mike");
		Accounts a = new Accounts();
		Command<User> u = Command.of(User.class, a );
		u.registerClassParser(User.class,  (param, text, context) -> new User(text));
		
		u.execute(mike, "increase 10");
		u.execute(john, "increase 20");
		u.execute(mike, "move 10 john");
		u.execute(john, "move 2 mike");
		
		assertEquals(a.credit.get(mike), 2);
		assertEquals(a.credit.get(john), 28);
	}

}
