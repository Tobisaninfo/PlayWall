package de.tobias.playwall.common.net;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public non-sealed class ResponseMessage extends BaseMessage
{
	protected ResponseMessage(UUID messageId)
	{
		super(messageId);
	}
}
