Command<Void> c = Command.of(this);Command<Void> c = Command.of(this);[![Release](https://jitpack.io/v/msx80/annocommand.svg)](https://jitpack.io/#msx80/annocommand)
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
		
		Command<Void> c = Command.of(new Simple());
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
Command<User> c = Command.of(User.class, new MyCommands());
...
@Cmd public void sayHello(User user, String someone)
{
}
...
c.execute(myUser, "sayHello Foobar");
```

You can customize other stuff, like how to convert from String to your custom objects, how to tokenize the strings, etc.
More advanced examples can be found in the test cases [here](https://github.com/msx80/annocommand/tree/master/src/test/java/org/github/msx80/annocommand).
