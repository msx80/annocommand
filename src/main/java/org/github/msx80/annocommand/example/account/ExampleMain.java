package org.github.msx80.annocommand.example.account;

import static org.github.msx80.annocommand.example.account.Role.*;

import org.github.msx80.annocommand.Command;

public class ExampleMain {

	public static String prepare(String text, Object[] passedParameters) {
		// let's strip starting /, or discard the message if it's not there (not a command)
		return text.startsWith("/") ? text.substring(1) : null;
	}

	
	public static void main(String[] args)
	{
		// this is the class implementing the commands
		ExampleCommands cmds = new ExampleCommands();
		
		// 2 is the number of passed parameters. This have to appear as the first two parameters of every @Cmd method
		Command c = new Command(2, cmds );
		
		// set up a processor for incoming commands
		c.setMessagePreparer(ExampleMain::prepare);  
		
		// tell how to convert from String to User
		c.registerClassParser(User.class,  (param, text, context) -> new User(text)); 
		
		// -- start of example
		
		User mike = new User("mike");
		User john = new User("john");
		User jack = new User("jack");
		
		c.execute("/hello", mike, ADMIN);
		c.execute("/hello Greeting to everybody!", mike, ADMIN);
		c.execute("/credit", john, USER);
		c.execute("/increase 10", john, USER);
		c.execute("/credit", john, USER);
		
		c.execute("/increase 100", jack, USER);
		c.execute("/credit", jack, USER);
		
		c.execute("/move 50 to john", jack, USER);
		c.execute("/credit", john, USER);
		
		c.execute("/list", mike, ADMIN);
	}
	
}
