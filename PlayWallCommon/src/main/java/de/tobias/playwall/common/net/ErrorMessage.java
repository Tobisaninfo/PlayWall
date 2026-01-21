package de.tobias.playwall.common.net;

import de.tobias.playwall.common.api.ServerError;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorMessage extends ResponseMessage
{
	private String message;
	private ServerError error;

	public ErrorMessage(UUID messageId, String message, ServerError error)
	{
		super(messageId);
		this.message = message;
		this.error = error;
	}
}
