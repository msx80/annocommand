package org.github.msx80.annocommand.example.account;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.github.msx80.annocommand.Cmd;

public class ExampleCommands {

	private Map<User, Integer> credit = new HashMap<>();
	
	@Cmd public void hello(User sender, Role role, String greeting)
	{
		System.out.println(sender+"> "+greeting);
	}
	
	@Cmd public void hello(User sender, Role role)
	{
		System.out.println(sender+"> Hello world");
	}
	
	@Cmd public void increase(User sender, Role role, Integer quantity)
	{
		accredit(sender, quantity);
		System.out.println(sender+"> done");
	}

	private void accredit(User account, Integer quantity) {
		Integer c = credit.getOrDefault(account, 0);
		credit.put(account, c+quantity);
	}
	
	@Cmd public void credit(User sender, Role role)
	{
		System.out.println(sender+"> credit is: "+credit.getOrDefault(sender, 0));
	}
	
	@Cmd public void list(User sender, Role role)
	{
		if(role != Role.ADMIN) throw new RuntimeException("Only admin can list");
		
		String accounts = credit
				.entrySet()
				.stream()
				.map(e -> "\t- "+e.getKey() +": "+e.getValue())
				.collect(Collectors.joining("\n"));
		
		System.out.println(sender+"> accounts are:\n"+accounts);
	}
	
	@Cmd public void move(User sender, Role role, Integer quantity, String _to, User recipient)
	{
		accredit(sender, -quantity);
		accredit(recipient, quantity);
		System.out.println(sender+"> moved "+quantity+" to "+recipient);
	}
	
	
}
