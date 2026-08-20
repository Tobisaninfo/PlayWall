package de.tobias.playwall.client.domain.project.view.settings;

import de.thecodelabs.midi.mapping.input.InputKey;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.mapping.KeyNameLocalizer;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class InputKeyLocalizer
{
	public static String localize(InputKey inputKey)
	{
		return switch(inputKey)
		{
			case KeyboardInputKey key ->
					Localization.getString(Strings.UI_SETTINGS_PROJECT_MAPPING_KEY_KEYBOARD, KeyNameLocalizer.getKeyName(key.code()));
			case MidiInputKey midi ->
					Localization.getString(Strings.UI_SETTINGS_PROJECT_MAPPING_KEY_MIDI, midi.value());
			default -> throw new IllegalStateException("Unexpected value: " + inputKey);
		};
	}
}
