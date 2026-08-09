package de.tobias.playwall.client.domain.project.view.settings;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.settings.SettingsCategory;
import de.tobias.playwall.client.view.settings.BaseSettingsDialogController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Viewcontroller for the project settings dialog.
 * Holds the sidebar consisting of {@link SettingsCategory} instances
 * and a container for showing the currently selected {@link de.tobias.playwall.client.view.components.settings.SettingsPage}
 * as well as the cancel and submit button.
 * On save all settings from all pages will be applied.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsView")
@Slf4j
public class ProjectSettingsViewController extends BaseSettingsDialogController<BaseProjectSettingsViewController.Param>
{
	@Getter(AccessLevel.NONE)
	private ProjectMetadata projectMetadata;

	@InjectConstructor
	public ProjectSettingsViewController(FluentClient client, ErrorAlertBuilder errorAlertBuilder)
	{
		super(client, errorAlertBuilder);
	}

	@Override
	protected void init()
	{
		boxCategories.getStyleClass().add("settings-category-box");

		final SettingsCategory categoryGeneral = createSettingsCategory(ProjectSettingsGeneralViewController.class, Strings.UI_SETTINGS_PROJECT_GENERAL_TITLE, FontAwesomeType.GEAR_SOLID);
		createSettingsCategory(ProjectSettingsDisplayViewController.class, Strings.UI_SETTINGS_PROJECT_VIEW_TITLE, FontAwesomeType.IMAGE_SOLID);
		createSettingsCategory(ProjectSettingsFadeViewController.class, Strings.UI_SETTINGS_PROJECT_FADE_TITLE, FontAwesomeType.SLIDERS_SOLID);
		createSettingsCategory(ProjectSettingsMappingViewController.class, Strings.UI_SETTINGS_PROJECT_MAPPING_TITLE, FontAwesomeType.KEYBOARD_SOLID);

		initButtons();

		selectCategory(categoryGeneral);
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);

		stage.setHeight(660);
		stage.setWidth(1040);
	}

	@Override
	public void initParameter(BaseProjectSettingsViewController.Param parameter)
	{
		this.projectMetadata = parameter.projectMetadata;

		settingViewController.forEach(controller -> controller.initParameter(new BaseProjectSettingsViewController.Param(projectMetadata)));
		stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PROJECT_TITLE, projectMetadata.getName()));
	}

	@FXML
	protected void saveButtonHandler(ActionEvent event)
	{
		settingViewController.forEach(controller -> controller.applySettings(new BaseProjectSettingsViewController.Param(projectMetadata)));

		try
		{
			client.currentProject().updateSettings(projectMetadata);
			getStageContainer().ifPresent(NVCStage::close);
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot update project settings", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROJECT_SETTINGS_SAVE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}
}
