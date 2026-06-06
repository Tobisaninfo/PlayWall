package de.tobias.playwall.client.domain.settings.view.settings;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.settings.SettingsCategory;
import de.tobias.playwall.client.view.settings.BaseSettingsDialogController;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDeviceInstance;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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

	@InjectConstructor
	public ProgramSettingsViewController(FluentClient client, ErrorAlertBuilder errorAlertBuilder)
	{
		super(client, errorAlertBuilder);
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
		settingViewController.forEach(controller -> controller.applySettings(new BaseProgramSettingsViewController.Param(settings, outputDevices)));

		try
		{
			client.settings().update(settings);
			getStageContainer().ifPresent(NVCStage::close);
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannit update programm settings", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROGRAM_SETTINGS_SAVE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}
}
