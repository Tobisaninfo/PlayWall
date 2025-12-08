package de.tobias.playwall.client.appcontext.loader;

import de.tobias.playwall.client.appcontext.AppContext;
import lombok.Getter;

@Getter
public class AppContextLoaderRequest
{
	private AppContext appContext;
	private String[] basePackages;
	private String[] rejectPackages = {};

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
