package de.tobias.playwall.client.di;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DI
{
	private final Map<Class<?>, Function<DI, ?>> supplier;
	private final Map<Class<?>, Object> objectCache;

	private static DI containerInstance;

	private DI()
	{
		supplier = new HashMap<>();
		objectCache = new HashMap<>();
	}

	public static DI instance()
	{
		if(containerInstance == null)
		{
			containerInstance = new DI();
		}
		return containerInstance;
	}

	public <T> void registerLazy(Class<T> clazz, Function<DI, T> function)
	{
		supplier.put(clazz, function);
	}

	public <T> void registerLazySingleton(Class<T> clazz, Function<DI, T> function)
	{
		supplier.put(clazz, (di) -> {
			if(objectCache.containsKey(clazz))
			{
				return objectCache.get(clazz);
			}
			final T instance = function.apply(di);
			objectCache.put(clazz, instance);
			return instance;
		});
	}

	public <T> T getInstance(Class<T> clazz) {
		return (T) supplier.get(clazz).apply(this);
	}
}
