package de.tobias.playwall.client.launch;

public class ServerLaunchException extends RuntimeException
{
	public ServerLaunchException(String message)
	{
		super(message);
	}

	public ServerLaunchException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
