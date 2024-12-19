package de.tobias.playwall.common.net;

import java.util.UUID;

public non-sealed class UpdateMessage extends BaseMessage
{
	protected UpdateMessage()
	{
		super(UUID.randomUUID());
	}
}
