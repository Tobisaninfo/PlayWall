package de.tobias.playwall.common.net;

import java.util.UUID;

public non-sealed class RequestMessage extends BaseMessage
{
	protected RequestMessage()
	{
		super(UUID.randomUUID());
	}
}
