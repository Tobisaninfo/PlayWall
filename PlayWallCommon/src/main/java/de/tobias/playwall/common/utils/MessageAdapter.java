package de.tobias.playwall.common.utils;

import com.google.gson.*;
import de.tobias.playwall.common.net.Message;
import de.tobias.playwall.common.net.Scope;

import java.lang.reflect.Type;

public class MessageAdapter implements JsonDeserializer<Message>
{
	@Override
	public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException
	{
		final JsonObject jsonObject = json.getAsJsonObject();
		final Scope scope = Scope.valueOf(jsonObject.get("scope").getAsString());

		return context.deserialize(jsonObject, scope.getClazz());
	}
}
