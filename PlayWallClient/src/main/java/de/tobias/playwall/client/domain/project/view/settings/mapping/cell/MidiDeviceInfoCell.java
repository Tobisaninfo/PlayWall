package de.tobias.playwall.client.domain.project.view.settings.mapping.cell;

import de.thecodelabs.midi.midi.device.MidiDeviceInfo;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.project.view.settings.mapping.ProjectSettingsMappingViewController;
import javafx.scene.control.ListCell;

public class MidiDeviceInfoCell extends ListCell<MidiDeviceInfoCell.MidiDeviceInfoCellData>
{
	public record MidiDeviceInfoCellData(String deviceName, MidiDeviceInfo deviceInfo, boolean isError)
	{
	}

	@Override
	protected void updateItem(MidiDeviceInfoCell.MidiDeviceInfoCellData item, boolean empty)
	{
		super.updateItem(item, empty);
		if(empty || item == null)
		{
			setText(null);
			setGraphic(null);
			return;
		}

		if(item.deviceName() == null)
		{
			setText(Localization.getString(Strings.UI_SETTINGS_PROJECT_MAPPING_MIDI_DEVICE_DISABLED));
		}
		else
		{
			setText(item.deviceName());
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
	}
}
