package de.tobias.playwall.client.net.responsehandler;

import de.tobias.playwall.common.net.EventType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ResponseHandlerService
{
	private static ResponseHandlerService instance;

	private final Map<EventType, ResponseHandler> handlers;

	private ResponseHandlerService()
	{
		this.handlers = new HashMap<>();
	}

	public static ResponseHandlerService getInstance()
	{
		if(instance == null)
		{
			instance = new ResponseHandlerService();
		}
		return instance;
	}

	public void addResponseHandler(EventType type, ResponseHandler handler)
	{
		handlers.put(type, handler);
	}

	public Optional<ResponseHandler> getResponseHandler(EventType eventType)
	{
		return Optional.ofNullable(handlers.get(eventType));
	}
}
