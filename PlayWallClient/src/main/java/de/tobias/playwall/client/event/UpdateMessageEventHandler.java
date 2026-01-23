package de.tobias.playwall.client.event;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.net.UpdateMessage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UpdateMessageEventHandler
{
	private final Map<Class<? extends UpdateMessage>, List<ListenerInstance>> listeners = new HashMap<>();

	public <T extends UpdateMessage> void registerListener(UpdateMessageEventListener<T> listener)
	{
		listeners.computeIfAbsent(listener.getMessageClass(), _ -> new ArrayList<>()).add(new InterfaceListenerInstance(listener));
	}

	public void registerListener(Object object)
	{
		for(Method method : object.getClass().getDeclaredMethods())
		{
			if(method.isAnnotationPresent(EventListener.class))
			{
				final EventListener eventListener = method.getAnnotation(EventListener.class);
				listeners.computeIfAbsent(eventListener.value(), _ -> new ArrayList<>()).add(new AnnotationListenerInstance(object, method));
			}
		}
	}

	public <T extends UpdateMessage> void unregisterListener(UpdateMessageEventListener<T> listener)
	{
		listeners.computeIfAbsent(listener.getMessageClass(), _ -> new ArrayList<>()).remove(new InterfaceListenerInstance(listener));
	}

	public void unregisterListener(Object object)
	{
		for(Method method : object.getClass().getDeclaredMethods())
		{
			if(method.isAnnotationPresent(EventListener.class))
			{
				final EventListener eventListener = method.getAnnotation(EventListener.class);
				listeners.computeIfAbsent(eventListener.value(), _ -> new ArrayList<>()).remove(new AnnotationListenerInstance(object, method));
			}
		}
	}

	public <T extends UpdateMessage> void fireEvent(T updateMessage)
	{
		final List<ListenerInstance> listenersForMessage = this.listeners.get(updateMessage.getClass());
		if(listenersForMessage == null)
		{
			return;
		}

		listenersForMessage.forEach(listener -> listener.execute(updateMessage));
	}
}
