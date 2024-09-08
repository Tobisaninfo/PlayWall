package de.tobias.playwall.client.net;

import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

// TODO: Singleton
public class MessageQueue
{
	private static MessageQueue instance;

	public static MessageQueue getInstance()
	{
		if(instance == null)
		{
			instance = new MessageQueue();
		}
		return instance;
	}

	private final Map<UUID, Consumer<ResponseMessage>> messagesQueue = new HashMap<>();

	public void enqueueCallback(RequestMessage message, Consumer<? extends ResponseMessage> callback)
	{
		messagesQueue.put(message.getMessageId(), (Consumer<ResponseMessage>) callback);
	}

	public Optional<Consumer<ResponseMessage>> dequeueCallback(UUID id)
	{
		return Optional.ofNullable(messagesQueue.get(id));
	}
}
