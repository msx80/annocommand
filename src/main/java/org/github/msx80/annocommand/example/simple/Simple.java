package org.github.msx80.annocommand.example.simple;

import org.github.msx80.annocommand.Cmd;
import org.github.msx80.annocommand.Command;

public class Simple {
	
	@Cmd public void hello(String someone)
	{
		System.out.println("Hello "+someone+"!");
	}

	@Cmd public void sum(int a, int b)
	{
		System.out.println("Sum is: "+(a+b));
	}

	@Cmd public String uppercase(String txt) 
	{
		return txt.toUpperCase();
	}
	
	public static void main(String[] args) {
		
		Command c = new Command(0, new Simple());
		
		c.execute("hello Johnny Cash");
		c.execute("sum 4 5");
		
		System.out.println(c.execute("uppercase sometext"));
	}

}
