package de.tobias.playwall.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tobias.playwall.common.net.Message;

public class GsonUtils
{
	private GsonUtils()
	{
	}

	public static Gson gson()
	{
		return gson(false);
	}

	public static Gson gson(boolean pretty)
	{
		return gsonBuilder(pretty).create();
	}

	public static GsonBuilder gsonBuilder(boolean pretty)
	{
		final GsonBuilder gsonBuilder = new GsonBuilder();

		if(pretty)
		{
			gsonBuilder.setPrettyPrinting();
		}

		gsonBuilder.registerTypeAdapter(Message.class, new MessageAdapter());

		return gsonBuilder;
	}
}
