package de.tobias.playwall.client.event;

import de.tobias.playwall.common.net.UpdateMessage;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

record AnnotationListenerInstance(Object instance, Method method) implements ListenerInstance
{
	@Override
	@SneakyThrows
	@SuppressWarnings("java:S3011")
	public void execute(UpdateMessage updateMessage)
	{
		method.setAccessible(true);
		method.invoke(instance, updateMessage);
	}
}
