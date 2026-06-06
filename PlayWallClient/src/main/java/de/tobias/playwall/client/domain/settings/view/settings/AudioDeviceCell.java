package de.tobias.playwall.client.domain.settings.view.settings;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDeviceInstance;
import javafx.scene.control.ListCell;

import java.text.MessageFormat;

class AudioDeviceCell extends ListCell<AudioDeviceInstance>
{
	@Override
	protected void updateItem(AudioDeviceInstance item, boolean empty)
	{
		super.updateItem(item, empty);

		if(empty || item == null)
		{
			setText(null);
			return;
		}

		if(item.isDefault())
		{
			setText(MessageFormat.format("{0} ({1})", item.name(), Localization.getString(Strings.UI_SETTINGS_PROGRAM_AUDIO_DEVICE_DEFAULT)));
			return;
		}

		setText(item.name());
	}
}