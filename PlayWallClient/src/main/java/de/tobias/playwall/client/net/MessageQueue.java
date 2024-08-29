package de.tobias.playwall.client.net;

import de.tobias.playwall.common.net.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

	private final List<Message> messagesQueue = new LinkedList<>();

	public void enqueue(Message message)
	{
		messagesQueue.add(message);
	}

	public Optional<Message> dequeue(UUID id)
	{
		final Optional<Message> res = messagesQueue.stream()
				.filter(message -> message.getMessageId().equals(id))
				.findAny();
		res.ifPresent(messagesQueue::remove);
		return res;
	}
}
