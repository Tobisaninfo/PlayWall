package de.tobias.playwall.client.appcontext;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("java:S6548")
public class AppContext
{
	public enum Environment
	{
		TESTING, GUI_TESTING, PRODUCTION
	}

	private final Map<Class<?>, Function<AppContext, ?>> supplier;
	private final Map<Class<?>, Object> objectCache;

	@Getter
	private final Environment environment;

	public AppContext(Environment environment)
	{
		this.environment = environment;
		supplier = new HashMap<>();
		objectCache = new HashMap<>();

		registerLazySingleton(AppContext.Environment.class, (_) -> environment);
	}

	public <T> void registerLazy(Class<T> clazz, Function<AppContext, T> function)
	{
		supplier.put(clazz, function);
	}

	public <T> void registerLazySingleton(Class<T> clazz, Function<AppContext, T> function)
	{
		supplier.put(clazz, context -> {
			if(objectCache.containsKey(clazz))
			{
				return objectCache.get(clazz);
			}
			final T instance = function.apply(context);
			objectCache.put(clazz, instance);
			return instance;
		});
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz)
	{
		final Function<AppContext, ?> function = supplier.get(clazz);
		if(function == null)
		{
			throw new ComponentNotFoundException("No component found for \"" + clazz.getName() + "\"");
		}
		return (T) function.apply(this);
	}
}
