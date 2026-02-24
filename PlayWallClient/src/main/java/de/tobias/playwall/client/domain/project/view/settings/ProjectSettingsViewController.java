package de.tobias.playwall.client.domain.project.view.settings;

import de.thecodelabs.logger.Logger;
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
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.settings.SettingsCategory;
import de.tobias.playwall.client.view.settings.BaseSettingsDialogController;
import de.tobias.playwall.client.view.settings.BaseSettingsViewController;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Viewcontroller for the project settings dialog.
 * Holds the sidebar consisting of {@link SettingsCategory} instances
 * and a container for showing the currently selected {@link de.tobias.playwall.client.view.components.settings.SettingsPage}
 * as well as the cancel and submit button.
 * On save all settings from all pages will be applied.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsView")
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

		initButtons();

		selectCategory(categoryGeneral);
	}

	@Override
	public void initParameter(BaseProjectSettingsViewController.Param parameter)
	{
		this.projectMetadata = parameter.projectMetadata;

		settingViewController.forEach(controller -> controller.initParameter(new BaseProjectSettingsViewController.Param(projectMetadata)));
		stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PROJECT_TITLE, projectMetadata.getName()));
	}

	@Override
	protected void initButtons()
	{
		final BooleanBinding allValidBinding = Bindings.createBooleanBinding(
				() -> settingViewController.stream()
						.allMatch(vc -> vc.getIsValidProperty().get()),
				settingViewController.stream()
						.map(BaseSettingsViewController::getIsValidProperty)
						.toArray(Observable[]::new)
		);

		saveButton.disableProperty().bind(allValidBinding.not());
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
			Logger.error(e.getMessage());
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROJECT_SETTINGS_SAVE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}
}
