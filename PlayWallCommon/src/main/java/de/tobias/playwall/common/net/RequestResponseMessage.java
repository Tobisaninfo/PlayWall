package de.tobias.playwall.common.net;

import com.google.gson.JsonObject;

import java.util.UUID;

public class RequestResponseMessage<T extends EventType> extends Message
{
	private T eventMessageType;

	// GSON only
	public RequestResponseMessage()
	{
		super();
		this.setMessageType(MessageType.REQUEST_RESPONSE);
	}

	public RequestResponseMessage(Scope scope, UUID messageId, T eventType)
	{
		this();
		this.setScope(scope);
		this.setMessageId(messageId);
		this.eventMessageType = eventType;
		this.object = new JsonObject();
	}

	public RequestResponseMessage(Scope scope, UUID messageId, T eventType, JsonObject object)
	{
		this();
		this.setScope(scope);
		this.setMessageId(messageId);
		this.eventMessageType = eventType;
		this.object = object;
	}

	public T getEventMessageType()
	{
		return eventMessageType;
	}

	@Override
	public String toString()
	{
		return "RequestResponseMessage{" +
				"eventMessageType=" + eventMessageType +
				", object=" + object +
				"} " + super.toString();
	}
}
