package de.tobias.playwall.iconoverview;

import de.thecodelabs.utils.util.Localization;

import java.util.Locale;

public class PlayWallIconOverviewLocalizationDelegate implements Localization.LocalizationDelegate
{
	@Override
	public String[] getBaseResources()
	{
		return new String[]{
				"de/tobias/playwall/iconoverview/localization/",
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
