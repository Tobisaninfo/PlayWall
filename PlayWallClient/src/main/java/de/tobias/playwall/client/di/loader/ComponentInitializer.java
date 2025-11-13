package de.tobias.playwall.client.di.loader;

import de.tobias.playwall.client.di.ComponentInitializationException;
import de.tobias.playwall.client.di.DI;
import de.tobias.playwall.client.di.InjectField;
import de.tobias.playwall.client.di.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.function.Function;

record ComponentInitializer<T>(Class<T> loadedClass, Constructor<T> injectConstructor) implements Function<DI, T>
{
	@Override
	public T apply(DI di)
	{
		try
		{
			final Class<?>[] paramTypes = injectConstructor.getParameterTypes();
			final Object[] params = new Object[paramTypes.length];
			for(int i = 0; i < paramTypes.length; i++)
			{
				params[i] = di.get(paramTypes[i]);
			}

			final T instance = injectConstructor.newInstance(params);
			injectFields(di, instance);
			return instance;
		}
		catch(Exception e)
		{
			throw new ComponentInitializationException(MessageFormat.format("Cannot instantiate component {0}", loadedClass), e);
		}
	}

	@SuppressWarnings("java:S3011")
	private static <T> void injectFields(DI di, T instance) throws IllegalAccessException
	{
		for(Field field : ReflectionUtils.getAllFields(instance.getClass()))
		{
			if(field.isAnnotationPresent(InjectField.class))
			{
				field.setAccessible(true);
				field.set(instance, di.get(field.getType()));
			}
		}
	}
}
