package de.tobias.playwall.common.net.project;

import com.google.gson.JsonObject;
import de.tobias.playwall.common.net.Message;
import de.tobias.playwall.common.net.RequestResponseMessage;
import de.tobias.playwall.common.net.Scope;

import java.util.UUID;

public class ProjectMessage extends RequestResponseMessage
{
	private ProjectEventMessageType eventMessageType;

	// GSON only
	public ProjectMessage()
	{
		super();
		setScope(Scope.PROJECT);
	}

	public ProjectMessage(UUID messageId, ProjectEventMessageType phaseEventMessageType)
	{
		this();
		this.setMessageId(messageId);
		this.eventMessageType = phaseEventMessageType;
		this.object = new JsonObject();
	}

	public ProjectMessage(UUID messageId, ProjectEventMessageType phaseEventMessageType, JsonObject object)
	{
		this();
		this.setMessageId(messageId);
		this.eventMessageType = phaseEventMessageType;
		this.object = object;
	}

	public ProjectEventMessageType getEventMessageType()
	{
		return eventMessageType;
	}

	@Override
	public String toString()
	{
		return "ProjectMessage{" +
				"eventMessageType=" + eventMessageType +
				", object=" + object +
				"}";
	}
}
