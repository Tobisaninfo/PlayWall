package de.tobias.playwall.client.domain.project.view.settings;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.settings.SettingsCategory;
import de.tobias.playwall.client.view.ParamDialogBase;
import de.tobias.playwall.client.view.settings.BaseSettingsViewController;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Viewcontroller for the project settings dialog.
 * Holds the sidebar consisting of {@link SettingsCategory} instances
 * and a container for showing the currently selected {@link de.tobias.playwall.client.view.components.settings.SettingsPage}
 * as well as the cancel and submit button.
 * On save all settings from all pages will be applied.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsView")
public class ProjectSettingsViewController extends ParamDialogBase<ProjectSettingsViewController.Param>
{
	@AllArgsConstructor
	public static class Param
	{
		private ProjectMetadata projectMetadata;
	}

	@FXML
	private VBox boxCategories;

	@FXML
	private VBox settingsPageContainer;

	@FXML
	private HBox boxButtons;

	@Getter(AccessLevel.NONE)
	private final FluentClient client;

	private final List<BaseProjectSettingsViewController> settingViewController = new ArrayList<>();

	@Getter(AccessLevel.NONE)
	private ProjectMetadata projectMetadata;

	private Stage stage;

	@InjectConstructor
	public ProjectSettingsViewController(FluentClient client)
	{
		this.client = client;
	}

	@Override
	protected void init()
	{
		boxCategories.getStyleClass().add("settings-category-box");

		final ProjectSettingsGeneralViewController projectSettingsGeneralViewController = AppContextHolder.getInstance().get(ProjectSettingsGeneralViewController.class);
		settingViewController.add(projectSettingsGeneralViewController);

		final SettingsCategory categoryGeneral = new SettingsCategory(Localization.getString(Strings.UI_SETTINGS_PROJECT_GENERAL_TITLE), FontAwesomeType.GEAR_SOLID, projectSettingsGeneralViewController);
		categoryGeneral.setOnAction(this::onSelectCategory);
		boxCategories.getChildren().add(categoryGeneral);

		final ProjectSettingsDisplayViewController projectSettingsDisplayViewController = AppContextHolder.getInstance().get(ProjectSettingsDisplayViewController.class);
		settingViewController.add(projectSettingsDisplayViewController);

		final SettingsCategory categoryView = new SettingsCategory(Localization.getString(Strings.UI_SETTINGS_PROJECT_VIEW_TITLE), FontAwesomeType.IMAGE_SOLID, projectSettingsDisplayViewController);
		categoryView.setOnAction(this::onSelectCategory);
		boxCategories.getChildren().add(categoryView);

		initButtons();

		selectCategory(categoryGeneral);
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);

		this.stage = stage;

		stage.setResizable(true);

		stage.setWidth(825);
		stage.setHeight(500);

		stage.setMinWidth(825);
		stage.setMinHeight(500);
	}

	private void onSelectCategory(ActionEvent event)
	{
		selectCategory((SettingsCategory) event.getSource());
	}

	private void selectCategory(SettingsCategory category)
	{
		boxCategories.getChildren().forEach(c -> c.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false));
		boxCategories.getChildren().stream()
				.filter(c -> c.equals(category))
				.findFirst()
				.ifPresent(c -> c.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true));

		settingsPageContainer.getChildren().setAll(category.getSettingsPageController().getSettingsPage());
	}

	@Override
	public void initParameter(Param param)
	{
		this.projectMetadata = param.projectMetadata;

		settingViewController.forEach(controller -> controller.initParameter(new BaseProjectSettingsViewController.Param(projectMetadata)));
		stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PROJECT_TITLE, projectMetadata.getName()));
	}

	private void initButtons()
	{
		final Button saveButton = new PlayWallButton(Localization.getString("ui.settings.button.save"), FontAwesomeType.FLOPPY_DISK_SOLID);
		saveButton.setId("saveButton");
		saveButton.setDefaultButton(true);
		saveButton.setOnAction(this::saveButtonHandler);

		final Button cancelButton = new PlayWallButton(Localization.getString("ui.settings.button.cancel"), FontAwesomeType.XMARK_SOLID);
		cancelButton.setId("cancelButton");
		cancelButton.setOnAction(this::cancelButtonHandler);

		boxButtons.getChildren().addAll(cancelButton, saveButton);

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
	private void saveButtonHandler(ActionEvent event)
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
			Alerts.getInstance().createAlert(Alert.AlertType.WARNING, null, e.getMessage(), getContainingWindow()).showAndWait();
		}
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event)
	{
		settingViewController.forEach(BaseSettingsViewController::cleanup);

		getStageContainer().ifPresent(NVCStage::close);
	}
}
