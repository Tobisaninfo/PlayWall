package de.tobias.playwall.client.domain.settings.view.settings;

import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDeviceInstance;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.stage.Modality;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Viewcontroller for the general page in the program settings dialog.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/program", view = "ProgramSettingsAudioPageView", applyToStage = false)
@SuppressWarnings("java:S110")
@Slf4j
public class ProgramSettingsAudioViewController extends BaseProgramSettingsViewController
{
	private static final AudioDeviceInstance AUDIO_DEVICE_USE_DEFAULT_FROM_OS = new AudioDeviceInstance(Localization.getString(Strings.UI_SETTINGS_PROGRAM_AUDIO_DEVICE_DEFAULT_ALWAYS), null, false, false);

	private final ClientProjectController projectController;

	@FXML
	private ComboBox<AudioDeviceInstance> comboBoxOutputDevices;

	@FXML
	private PlayWallButton playTestSoundButton;

	private boolean isTestSoundPlaying = false;

	@InjectConstructor
	public ProgramSettingsAudioViewController(FluentClient client, ClientProjectController projectController)
	{
		super(client);
		this.projectController = projectController;
	}

	@Override
	public void initParameter(Param param)
	{
		final List<AudioDeviceInstance> audioDevices = new ArrayList<>(param.getOutputDevices());
		audioDevices.addFirst(AUDIO_DEVICE_USE_DEFAULT_FROM_OS);

		comboBoxOutputDevices.getItems().setAll(audioDevices);
		comboBoxOutputDevices.setButtonCell(new AudioDeviceCell());
		comboBoxOutputDevices.setCellFactory(_ -> new AudioDeviceCell());
		comboBoxOutputDevices.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
			if(isTestSoundPlaying)
			{
				stopTestSound();
			}
		});

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

			if(selectedInstanceOptional.isPresent())
			{
				comboBoxOutputDevices.getSelectionModel().select(selectedInstanceOptional.get());
			}
			else
			{
				final AudioDeviceInstance errorDevice = new AudioDeviceInstance(selectedAudioDevice, null, false, true);
				comboBoxOutputDevices.getItems().add(errorDevice);
				comboBoxOutputDevices.getSelectionModel().select(errorDevice);
			}
		}

		this.isValidProperty.set(true);
	}

	@Override
	public boolean shouldApplySettingsAbort(Param param)
	{
		final String previousSelectedAudioDevice = param.getSettings().getSelectedAudioDevice();
		final String currentSelectedAudioDevice = getSelectedAudioDeviceName();

		if(!Objects.equals(previousSelectedAudioDevice, currentSelectedAudioDevice) && projectController.isAtLeastOnePadPlaying())
		{
			return showPlayingPadsWarningAlert();
		}
		return false;
	}

	@Override
	public void applySettings(Param param)
	{
		param.getSettings().setSelectedAudioDevice(getSelectedAudioDeviceName());

		if(isTestSoundPlaying)
		{
			stopTestSound();
		}
	}

	private boolean showPlayingPadsWarningAlert()
	{
		final Alert alert = Alerts.getInstance().createAlert(Alert.AlertType.WARNING, Localization.getString(Strings.UI_NOTIFICATION_WARNING), Localization.getString(Strings.UI_SETTINGS_PROGRAM_AUDIO_DEVICE_WARNING_PLAYING_PADS), null, getContainingWindow());
		alert.getButtonTypes().clear();
		alert.getButtonTypes().add(new ButtonType(Localization.getString("ui.settings.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE));
		alert.getButtonTypes().add(new ButtonType(Localization.getString("ui.settings.program.audio.device.warning.playing.pads.button.save"), ButtonBar.ButtonData.OK_DONE));
		getStageContainer().ifPresent(nvcStage -> alert.initOwner(nvcStage.getStage()));
		alert.initModality(Modality.WINDOW_MODAL);

		final Optional<ButtonType> response = alert.showAndWait();
		if(response.filter(button -> button.getButtonData() == ButtonBar.ButtonData.OK_DONE).isPresent())
		{
			try
			{
				client.currentProject().stopAllPadsImmediately();
			}
			catch(PlayWallApiException e)
			{
				log.error("Cannot stop all playing pad", e);
			}

			return false;
		}

		return true;
	}

	private String getSelectedAudioDeviceName()
	{
		final AudioDeviceInstance selectedItem = comboBoxOutputDevices.getSelectionModel().getSelectedItem();
		if(selectedItem == null || selectedItem == AUDIO_DEVICE_USE_DEFAULT_FROM_OS)
		{
			return null;
		}
		else
		{
			return selectedItem.name();
		}
	}

	@Override
	public void cleanup()
	{
		if(isTestSoundPlaying)
		{
			stopTestSound();
		}
	}

	@FXML
	public void playTestSoundHandler(ActionEvent event)
	{
		if(isTestSoundPlaying)
		{
			stopTestSound();
			return;
		}

		try
		{
			client.playTestSound(getSelectedAudioDeviceName());
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot play test sound", e);
		}
		playTestSoundButton.setText(Localization.getString(Strings.UI_SETTINGS_PROGRAM_AUDIO_DEVICE_TEST_SOUND_STOP));
		playTestSoundButton.setIcon(FontAwesomeType.STOP_SOLID);
		isTestSoundPlaying = true;
	}

	private void stopTestSound()
	{
		try
		{
			client.stopTestSound();
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot stop test sound", e);
		}

		playTestSoundButton.setText(Localization.getString(Strings.UI_SETTINGS_PROGRAM_AUDIO_DEVICE_TEST_SOUND_PLAY));
		playTestSoundButton.setIcon(FontAwesomeType.PLAY_SOLID);
		isTestSoundPlaying = false;
	}
}
