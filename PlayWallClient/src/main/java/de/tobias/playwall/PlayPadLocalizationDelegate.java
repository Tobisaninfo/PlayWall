package de.tobias.playwall;

import de.thecodelabs.utils.util.Localization;

import java.util.Locale;

public class PlayPadLocalizationDelegate implements Localization.LocalizationDelegate
{
	@Override
	public String[] getBaseResources()
	{
		return new String[]{
				"de/tobias/playwall/localization/",
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
