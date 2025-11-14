package de.tobias.playwall.client.appcontext.loader;

import de.tobias.playwall.client.appcontext.ComponentInitializationException;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.InjectField;
import de.tobias.playwall.client.appcontext.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.function.Function;

record ComponentInitializer<T>(Class<T> loadedClass,
							   Constructor<T> injectConstructor) implements Function<AppContext, T>
{
	@Override
	public T apply(AppContext context)
	{
		try
		{
			final T instance = constructInstance(context);
			injectFields(context, instance);
			return instance;
		}
		catch(Exception e)
		{
			throw new ComponentInitializationException(MessageFormat.format("Cannot instantiate component {0}", loadedClass), e);
		}
	}

	private T constructInstance(AppContext context) throws InstantiationException, IllegalAccessException, InvocationTargetException
	{
		final Class<?>[] paramTypes = injectConstructor.getParameterTypes();
		final Object[] params = new Object[paramTypes.length];
		for(int i = 0; i < paramTypes.length; i++)
		{
			params[i] = context.get(paramTypes[i]);
		}
		return injectConstructor.newInstance(params);
	}

	@SuppressWarnings("java:S3011")
	private void injectFields(AppContext context, T instance) throws IllegalAccessException
	{
		for(Field field : ReflectionUtils.getAllFields(instance.getClass()))
		{
			if(field.isAnnotationPresent(InjectField.class))
			{
				field.setAccessible(true);
				field.set(instance, context.get(field.getType()));
			}
		}
	}
}
