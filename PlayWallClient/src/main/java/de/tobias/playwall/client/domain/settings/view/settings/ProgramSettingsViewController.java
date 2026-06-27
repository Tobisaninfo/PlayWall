package de.tobias.playwall.client.domain.settings.view.settings;

import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.settings.SettingsCategory;
import de.tobias.playwall.client.view.settings.BaseSettingsDialogController;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDeviceInstance;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Viewcontroller for the program settings dialog.
 * Holds the sidebar consisting of {@link SettingsCategory} instances
 * and a container for showing the currently selected {@link de.tobias.playwall.client.view.components.settings.SettingsPage}
 * as well as the cancel and submit button.
 * On save all settings from all pages will be applied.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/program", view = "ProgramSettingsView")
@Slf4j
public class ProgramSettingsViewController extends BaseSettingsDialogController<BaseProgramSettingsViewController.Param>
{
	@Getter(AccessLevel.NONE)
	private Settings settings;

	@Getter(AccessLevel.NONE)
	private List<AudioDeviceInstance> outputDevices;

	private final ClientProjectController projectController;

	@InjectConstructor
	public ProgramSettingsViewController(FluentClient client, ErrorAlertBuilder errorAlertBuilder, ClientProjectController projectController)
	{
		super(client, errorAlertBuilder);
		this.projectController = projectController;
	}

	@Override
	protected void init()
	{
		boxCategories.getStyleClass().add("settings-category-box");

		final SettingsCategory categoryGeneral = createSettingsCategory(ProgramSettingsGeneralViewController.class, Strings.UI_SETTINGS_PROGRAM_GENERAL_TITLE, FontAwesomeType.GEAR_SOLID);
		createSettingsCategory(ProgramSettingsAudioViewController.class, Strings.UI_SETTINGS_PROGRAM_AUDIO_TITLE, FontAwesomeType.VOLUME_HIGH_SOLID);

		initButtons();

		selectCategory(categoryGeneral);
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);

		stage.setWidth(875);
		stage.setMinWidth(875);
	}

	@Override
	public void initParameter(BaseProgramSettingsViewController.Param parameter)
	{
		this.settings = parameter.settings;
		this.outputDevices = parameter.outputDevices;

		settingViewController.forEach(controller -> controller.initParameter(new BaseProgramSettingsViewController.Param(settings, outputDevices)));

		stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PROGRAM_TITLE));
	}

	@FXML
	protected void saveButtonHandler(ActionEvent event)
	{
		final String previousSelectedAudioDevice = settings.getSelectedAudioDevice();
		settingViewController.forEach(controller -> controller.applySettings(new BaseProgramSettingsViewController.Param(settings, outputDevices)));
		final String currentSelectedAudioDevice = settings.getSelectedAudioDevice();

		if(!Objects.equals(previousSelectedAudioDevice, currentSelectedAudioDevice) && projectController.isAtLeastOnePadPlaying())
		{
			final boolean preventSave = showPlayingPadsWarningAlert();
			if(preventSave)
			{
				settings.setSelectedAudioDevice(previousSelectedAudioDevice);
				return;
			}
		}

		try
		{
			client.settings().update(settings);
			getStageContainer().ifPresent(NVCStage::close);
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot update programm settings", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROGRAM_SETTINGS_SAVE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
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
				client.currentProject().stopAllPads();
			}
			catch(PlayWallApiException e)
			{
				log.error("Cannot stop all playing pad", e);
			}

			return false;
		}

		return true;
	}
}
