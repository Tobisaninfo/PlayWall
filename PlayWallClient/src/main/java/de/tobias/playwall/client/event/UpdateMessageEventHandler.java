package de.tobias.playwall.client.event;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.net.UpdateMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings({"rawtypes", "unchecked"})
public class UpdateMessageEventHandler
{
	private final Map<Class<? extends UpdateMessage>, List<UpdateMessageEventListener>> listeners = new HashMap<>();

	public <T extends UpdateMessage> void registerListener(UpdateMessageEventListener<T> listener)
	{
		listeners.computeIfAbsent(listener.getMessageClass(), _ -> new ArrayList<>()).add(listener);
	}

	public <T extends UpdateMessage> void unregisterListener(UpdateMessageEventListener<T> listener)
	{
		listeners.computeIfAbsent(listener.getMessageClass(), _ -> new ArrayList<>()).remove(listener);
	}

	public <T extends UpdateMessage> void fireEvent(T updateMessage)
	{
		final List<UpdateMessageEventListener> listenersForMessage = this.listeners.get(updateMessage.getClass());
		if(listenersForMessage == null)
		{
			return;
		}

		listenersForMessage.forEach(listener -> listener.onUpdateMessage(updateMessage));
	}
}
