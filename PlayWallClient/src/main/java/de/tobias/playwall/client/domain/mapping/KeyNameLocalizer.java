package de.tobias.playwall.client.domain.mapping;

import de.thecodelabs.utils.util.Localization;
import javafx.scene.input.KeyCode;

public final class KeyNameLocalizer
{
	private static final String UI_KEY_BASE = "ui.key.";

	private KeyNameLocalizer()
	{
	}

	public static String getKeyName(KeyCode code)
	{
		final String key = UI_KEY_BASE + code.name().toLowerCase();

		try
		{
			final String localized = Localization.getString(key);
			if(localized != null && !localized.isBlank() && !localized.equals(key))
			{
				return localized;
			}
		}
		catch(Exception _)
		{
			// Fall back to the default name
		}

		return code.getName();
	}
}
