package de.tobias.playwall.client.domain.settings.view.settings;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDeviceInstance;
import javafx.scene.control.ListCell;

import java.util.ArrayList;
import java.util.List;

class AudioDeviceCell extends ListCell<AudioDeviceInstance>
{
	@Override
	protected void updateItem(AudioDeviceInstance item, boolean empty)
	{
		super.updateItem(item, empty);

		if(empty || item == null)
		{
			setText(null);
			setGraphic(null);
			return;
		}

		if(item.isError())
		{
			final FontIcon icon = new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID);
			icon.getStyleClass().add("warning");
			setGraphic(icon);
		}
		else
		{
			setGraphic(null);
		}

		final List<String> details = new ArrayList<>();
		if(item.driver() != null && !item.driver().isBlank())
		{
			details.add(item.driver());
		}
		if(item.isDefault())
		{
			details.add(Localization.getString(Strings.UI_SETTINGS_PROGRAM_AUDIO_DEVICE_DEFAULT));
		}

		String label = item.name();
		if(!details.isEmpty())
		{
			label += " (" + String.join(", ", details) + ")";
		}
		setText(label);
	}
}