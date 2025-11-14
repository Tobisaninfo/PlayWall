package de.tobias.playwall.client.appcontext;

public class ComponentInitializationException extends RuntimeException
{
	public ComponentInitializationException(String message)
	{
		super(message);
	}

	public ComponentInitializationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
