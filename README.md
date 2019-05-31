# annocommand
Annotation based text command parser

This is a simple utility that takes textual commands in the form "command param1 param2 etc." and automatically matches it to method calls on specified objects.

# example

```java
package org.github.msx80.annocommand.example.simple;

import org.github.msx80.annocommand.*;

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
		
		c.execute("hello Johnny");
		c.execute("sum 4 5");
		
		System.out.println(c.execute("uppercase sometext"));
	}

}
```

This will print:

```
Hello Johnny!
Sum is: 9
SOMETEXT
```

You can also specify some context object that you pass to `execute`, for example to handle users or sessions:

```java
Command c = new Command(2, new MyCommands()); // 2 is the number of context parameters
...
@Cmd public void sayHello(User user, Session session, String someone)
{
}
...
c.execute(myUser, mySession, "sayHello Foobar");
```

You can customize other stuff, like how to convert from String to your custom objects, how to tokenize the strings, etc.
