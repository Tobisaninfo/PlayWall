package de.tobias.playwall.server;

import de.thecodelabs.utils.util.Localization;

import java.util.Locale;

public class PlayWallLocalizationDelegate implements Localization.LocalizationDelegate
{
	@Override
	public String[] getBaseResources()
	{
		return new String[]{
				"de/tobias/playwall/server/localization/",
		};
	}

	@Override
	public boolean useMultipleResourceBundles()
	{
		return true;
	}

	@Override
	public Locale getLocale()
	{
		return Locale.GERMAN;
	}
}
