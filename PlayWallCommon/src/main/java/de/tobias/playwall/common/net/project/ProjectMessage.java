package de.tobias.playwall.common.net.project;

import com.google.gson.JsonObject;
import de.tobias.playwall.common.net.Message;
import de.tobias.playwall.common.net.Scope;

public class ProjectMessage extends Message
{
	private ProjectEventMessageType eventMessageType;

	// GSON only
	public ProjectMessage()
	{
		setScope(Scope.PROJECT);
	}

	public ProjectMessage(ProjectEventMessageType phaseEventMessageType)
	{
		this();
		this.eventMessageType = phaseEventMessageType;
		this.object = new JsonObject();
	}

	public ProjectMessage(ProjectEventMessageType phaseEventMessageType, JsonObject object)
	{
		this();
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
