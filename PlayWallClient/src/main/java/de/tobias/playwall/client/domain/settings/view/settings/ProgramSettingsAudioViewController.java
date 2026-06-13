package de.tobias.playwall.client.domain.settings.view.settings;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDeviceInstance;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Viewcontroller for the general page in the program settings dialog.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/program", view = "ProgramSettingsAudioPageView", applyToStage = false)
@SuppressWarnings("java:S110")
public class ProgramSettingsAudioViewController extends BaseProgramSettingsViewController
{
	private static final AudioDeviceInstance AUDIO_DEVICE_USE_DEFAULT_FROM_OS = new AudioDeviceInstance(Localization.getString(Strings.UI_SETTINGS_PROGRAM_AUDIO_DEVICE_DEFAULT_ALWAYS), false);

	@FXML
	private ComboBox<AudioDeviceInstance> comboBoxOutputDevices;

	@InjectConstructor
	public ProgramSettingsAudioViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		final List<AudioDeviceInstance> audioDevices = new ArrayList<>(param.getOutputDevices());
		audioDevices.addFirst(AUDIO_DEVICE_USE_DEFAULT_FROM_OS);

		comboBoxOutputDevices.getItems().setAll(audioDevices);
		comboBoxOutputDevices.setButtonCell(new AudioDeviceCell());
		comboBoxOutputDevices.setCellFactory(_ -> new AudioDeviceCell());

		final String selectedAudioDevice = param.getSettings().getSelectedAudioDevice();
		if(selectedAudioDevice == null)
		{
			comboBoxOutputDevices.getSelectionModel().select(AUDIO_DEVICE_USE_DEFAULT_FROM_OS);
		}
		else
		{
			final Optional<AudioDeviceInstance> selectedInstanceOptional = param.outputDevices.stream()
					.filter(d -> d.name().equals(selectedAudioDevice))
					.findFirst();

			selectedInstanceOptional.ifPresent(audioDeviceInstance -> comboBoxOutputDevices.getSelectionModel().select(audioDeviceInstance));
		}

		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
		final AudioDeviceInstance selectedItem = comboBoxOutputDevices.getSelectionModel().getSelectedItem();
		if(selectedItem == null || selectedItem == AUDIO_DEVICE_USE_DEFAULT_FROM_OS)
		{
			param.getSettings().setSelectedAudioDevice(null);
		}
		else
		{
			param.getSettings().setSelectedAudioDevice(selectedItem.name());
		}
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}
}
