package de.tobias.playwall.common.net;


import com.google.gson.*;
import de.tobias.playwall.common.utils.GsonUtils;

import java.lang.reflect.Type;
import java.util.UUID;

@SuppressWarnings("rawtypes")
public abstract class Message
{
	private static final Gson GSON = GsonUtils.gson();

	private UUID messageId;
	private MessageType messageType;

	private Scope scope;
	protected JsonObject object;

	public UUID getMessageId()
	{
		return messageId;
	}

	public void setMessageId(UUID messageId)
	{
		this.messageId = messageId;
	}

	public MessageType getMessageType()
	{
		return messageType;
	}

	public void setMessageType(MessageType messageType)
	{
		this.messageType = messageType;
	}

	public boolean containsKey(Enum key)
	{
		return object.has(key.name());
	}

	public Scope getScope()
	{
		return scope;
	}

	public void setScope(Scope scope)
	{
		this.scope = scope;
	}

	public JsonObject getObject()
	{
		return object;
	}

	public void addPayload(Enum key, Enum data)
	{
		addPayload(key, new JsonPrimitive(data.name()));
	}

	public void addPayload(Enum key, String data)
	{
		if(data == null)
		{
			data = "";
		}
		addPayload(key, new JsonPrimitive(data));
	}

	public void addPayload(Enum key, Number data)
	{
		addPayload(key, new JsonPrimitive(data));
	}

	public void addPayload(Enum key, boolean data)
	{
		addPayload(key, new JsonPrimitive(data));
	}

	public void addPayload(Enum key, JsonElement data)
	{
		addPayload(key.name(), data);
	}

	public void addPayload(Enum key, Object data)
	{
		addPayload(key.name(), GSON.toJsonTree(data));
	}

	public <T> void addPayload(Enum key, T data, Class<T> type)
	{
		addPayload(key.name(), GSON.toJsonTree(data, type));
	}

	public void addPayload(String key, JsonElement data)
	{
		object.add(key, data);
	}


	public String getString(Enum key)
	{
		return getString(key.name());
	}

	public Number getNumber(Enum key)
	{
		return getNumber(key.name());
	}

	public boolean getBoolean(Enum key)
	{
		return getBoolean(key.name());
	}

	public <T extends Enum<T>> T getEnumValue(Class<T> clazz, Enum key)
	{
		return getEnumValue(clazz, key.name());
	}

	public JsonObject getObject(Enum key)
	{
		return getObject(key.name());
	}

	public JsonArray getArray(Enum key)
	{
		return getArray(key.name());
	}

	public <T> T getArray(Enum key, Class<T> type)
	{
		return GSON.fromJson(getArray(key), type);
	}

	public <T> T getObject(Enum key, Class<T> type)
	{
		return GSON.fromJson(getObject(key), type);
	}

	public <T> T getArray(Enum key, Type type)
	{
		return GSON.fromJson(getArray(key), type);
	}

	public <T> T getObject(Enum key, Type type)
	{
		return GSON.fromJson(getObject(key), type);
	}

	public String getString(String key)
	{
		final JsonPrimitive element = object.getAsJsonPrimitive(key);
		if(element == null)
		{
			return null;
		}
		return element.getAsString();
	}

	public Number getNumber(String key)
	{
		return object.getAsJsonPrimitive(key).getAsNumber();
	}

	public boolean getBoolean(String key)
	{
		return object.getAsJsonPrimitive(key).getAsBoolean();
	}

	public <T extends Enum<T>> T getEnumValue(Class<T> clazz, String key)
	{
		final JsonPrimitive element = object.getAsJsonPrimitive(key);
		if(element == null)
		{
			return null;
		}
		return Enum.valueOf(clazz, element.getAsString());
	}

	public JsonObject getObject(String key)
	{
		return object.getAsJsonObject(key);
	}

	public JsonArray getArray(String key)
	{
		return object.getAsJsonArray(key);
	}

	@Override
	public String toString()
	{
		return "Message{" +
				"messageId=" + messageId +
				", messageType=" + messageType +
				", scope=" + scope +
				", object=" + object +
				'}';
	}
}
