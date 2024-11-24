package de.tobias.playwall.client;

import de.tobias.playwall.common.api.ServerError;
import lombok.Getter;

@Getter
public class PlayWallApiException extends Exception
{
	private final ServerError error;

	public PlayWallApiException(String message, ServerError error)
	{
		super(message);
		this.error = error;
	}
}
