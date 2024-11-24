package de.tobias.playwall.server.api;

import de.tobias.playwall.common.api.ServerError;
import lombok.Getter;

@Getter
public class PlayWallServerException extends Exception
{
	private final ServerError error;

	public PlayWallServerException(String message, ServerError error)
	{
		super(message);
		this.error = error;
	}
}
