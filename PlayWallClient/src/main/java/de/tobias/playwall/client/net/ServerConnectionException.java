package de.tobias.playwall.client.net;

public class ServerConnectionException extends RuntimeException
{
	public ServerConnectionException(String message)
	{
		super(message);
	}

	public ServerConnectionException(Throwable cause)
	{
		super(cause);
	}
}
