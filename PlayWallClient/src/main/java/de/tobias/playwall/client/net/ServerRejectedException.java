package de.tobias.playwall.client.net;

public class ServerRejectedException extends ServerConnectionException
{
	public ServerRejectedException(String reason)
	{
		super("Server rejected connection: " + reason);
	}
}
