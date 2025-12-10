package de.tobias.playwall.client.appcontext.loader;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.function.Function;

public class ComponentInitializer<T> implements Function<AppContext, T>
{
	private final Class<T> loadedClass;
	private final Constructor<T> injectConstructor;

	protected ComponentInitializer(Class<T> loadedClass, Constructor<T> injectConstructor)
	{
		this.loadedClass = loadedClass;
		this.injectConstructor = injectConstructor;
	}

	@Override
	public T apply(AppContext context)
	{
		try
		{
			final T instance = constructInstance(context);
			injectFields(context, instance);
			loadView(instance, context);
			executePostConstruct(instance);
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

	private void loadView(T instance, AppContext context)
	{
		final Class<?> instanceClass = instance.getClass();
		if(instanceClass.isAnnotationPresent(ViewController.class))
		{
			final ViewController annotation = instanceClass.getAnnotation(ViewController.class);
			if(annotation.autoload())
			{
				final NVC nvc = (NVC) instance;
				nvc.load(annotation.path(), annotation.view(), Localization.getBundle());
				applyViewToStage(context, annotation, nvc);
			}
		}
	}

	protected void applyViewToStage(AppContext context, ViewController annotation, NVC nvc)
	{
		if(annotation.applyToStage())
		{
			nvc.applyViewControllerToStage();
		}
	}

	@SuppressWarnings("java:S3011")
	private void executePostConstruct(T instance) throws InvocationTargetException, IllegalAccessException
	{
		for(Method method : ReflectionUtils.getAllMethods(instance.getClass()))
		{
			if(method.isAnnotationPresent(PostConstruct.class))
			{
				method.setAccessible(true);
				method.invoke(instance);
			}
		}
	}
}
