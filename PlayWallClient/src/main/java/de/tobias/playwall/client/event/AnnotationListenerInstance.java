package de.tobias.playwall.client.event;

import de.tobias.playwall.common.net.UpdateMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

record AnnotationListenerInstance(Object instance, Method method) implements ListenerInstance
{
	@Override
	@SuppressWarnings("java:S3011")
	public void execute(UpdateMessage updateMessage)
	{
		method.setAccessible(true);
		try
		{
			method.invoke(instance, updateMessage);
		}
		catch(IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException(e.getCause());
		}
	}
}
