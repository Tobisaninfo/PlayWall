package de.tobias.playwall.client;

import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.localization.LocalizationMessageFormatter;

import java.text.MessageFormat;
import java.util.Locale;

public class PlayWallLocalizationDelegate implements Localization.LocalizationDelegate
{
	@Override
	public String[] getBaseResources()
	{
		return new String[]{
				"de/tobias/playwall/client/localization/",
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

	@Override
	public LocalizationMessageFormatter messageFormatter()
	{
		return (localizationKey, objects) -> {
			final MessageFormat fmt = new MessageFormat(localizationKey, Locale.GERMAN);
			return fmt.format(objects);
		};
	}
}
