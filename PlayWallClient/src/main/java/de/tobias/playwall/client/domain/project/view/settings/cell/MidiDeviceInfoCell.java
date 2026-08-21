package de.tobias.playwall.client.domain.project.view.settings.cell;

import de.thecodelabs.midi.midi.device.MidiDeviceInfo;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import javafx.scene.control.ListCell;

public class MidiDeviceInfoCell extends ListCell<MidiDeviceInfo>
{
	@Override
	protected void updateItem(MidiDeviceInfo item, boolean empty)
	{
		super.updateItem(item, empty);
		if(empty || item == null)
		{
			setText(Localization.getString(Strings.UI_SETTINGS_PROJECT_MAPPING_MIDI_DEVICE_DISABLED));
		}
		else
		{
			setText(item.displayName());
		}
	}
}
