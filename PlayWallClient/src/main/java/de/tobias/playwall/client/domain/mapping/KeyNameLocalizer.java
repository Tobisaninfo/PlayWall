package de.tobias.playwall.client.domain.mapping;

import de.thecodelabs.utils.util.Localization;
import javafx.scene.input.KeyCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class KeyNameLocalizer
{
	private static final String UI_KEY_BASE = "ui.key.";

	public static String getKeyName(KeyCode code)
	{
		final String key = UI_KEY_BASE + code.name().toLowerCase();

		final String localized = Localization.getString(key);
		if(localized != null && !localized.isBlank() && !localized.equals(key))
		{
			return localized;
		}

		return code.getName();
	}
}
