package de.tobias.playwall.client.net;

import de.tobias.playwall.common.net.BaseMessage;

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

	private final Map<UUID, Consumer<BaseMessage>> messagesQueue = new HashMap<>();

	public void enqueueCallback(BaseMessage message, Consumer<? extends BaseMessage> callback)
	{
		messagesQueue.put(message.getMessageId(), (Consumer<BaseMessage>) callback);
	}

	public Optional<Consumer<BaseMessage>> dequeueCallback(UUID id)
	{
		return Optional.ofNullable(messagesQueue.get(id));
	}
}
