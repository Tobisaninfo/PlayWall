package de.tobias.playwall.client.net;

import de.tobias.playwall.common.net.ResponseMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// TODO: Singleton
public class ResponseQueue
{
	private static ResponseQueue instance;

	public static ResponseQueue getInstance()
	{
		if(instance == null)
		{
			instance = new ResponseQueue();
		}
		return instance;
	}

	private final Map<UUID, ResponseMessage> responseQueueMap = new HashMap<>();

	public void enqueueResponse(UUID requestId, ResponseMessage response)
	{
		responseQueueMap.put(requestId, response);
	}

	public Optional<ResponseMessage> dequeueResponse(UUID id)
	{
		return Optional.ofNullable(responseQueueMap.get(id));
	}
}
