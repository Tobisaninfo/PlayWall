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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

		initButtons();

		selectCategory(categoryGeneral);
	}

	@Override
	public void initParameter(BaseProgramSettingsViewController.Param parameter)
	{
		this.settings = parameter.settings;

		settingViewController.forEach(controller -> controller.initParameter(new BaseProgramSettingsViewController.Param(settings)));

		stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PROGRAM_TITLE));
	}

	@FXML
	protected void saveButtonHandler(ActionEvent event)
	{
		settingViewController.forEach(controller -> controller.applySettings(new BaseProgramSettingsViewController.Param(settings)));

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
