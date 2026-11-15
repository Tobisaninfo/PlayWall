	package de.tobias.playwall.client.update;

public class UpdateCheckException extends Exception
{
	public UpdateCheckException(String message)
	{
		super(message);
	}

	public UpdateCheckException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
