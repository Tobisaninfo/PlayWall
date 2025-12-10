package de.tobias.playwall.client.appcontext.loader;

import de.tobias.playwall.client.appcontext.AppContext;
import lombok.Getter;

import java.lang.reflect.Constructor;

@Getter
public class AppContextLoaderRequest
{
	public interface ComponentInitializerSupplier<T>
	{
		ComponentInitializer<T> create(Class<T> loadedClass, Constructor<T> injectConstructor);
	}

	private ComponentInitializerSupplier<?> componentInitializer = ComponentInitializer::new;
	private AppContext appContext;
	private String[] basePackages;
	private String[] rejectPackages = {};


	public AppContextLoaderRequest withComponentInitializer(ComponentInitializerSupplier<?> componentInitializer)
	{
		this.componentInitializer = componentInitializer;
		return this;
	}

	public AppContextLoaderRequest withAppContext(AppContext appContext)
	{
		this.appContext = appContext;
		return this;
	}

	public AppContextLoaderRequest withBasePackages(String... basePackages)
	{
		this.basePackages = basePackages;
		return this;
	}

	public AppContextLoaderRequest withRejectPackages(String... rejectPackages)
	{
		this.rejectPackages = rejectPackages;
		return this;
	}
}
