package de.tobias.playwall.client.domain.settings.view.settings;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.ParamDialogBase;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.settings.SettingsCategory;
import de.tobias.playwall.client.view.settings.BaseSettingsViewController;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
 * Viewcontroller for the program settings dialog.
 * Holds the sidebar consisting of {@link SettingsCategory} instances
 * and a container for showing the currently selected {@link de.tobias.playwall.client.view.components.settings.SettingsPage}
 * as well as the cancel and submit button.
 * On save all settings from all pages will be applied.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/program", view = "ProgramSettingsView")
public class ProgramSettingsViewController extends ParamDialogBase<ProgramSettingsViewController.Param>
{
	@AllArgsConstructor
	public static class Param
	{
		private Settings settings;
	}

	@FXML
	private VBox boxCategories;

	@FXML
	private VBox settingsPageContainer;

	@FXML
	private HBox boxButtons;

	@Getter(AccessLevel.NONE)
	private final FluentClient client;

	private final List<BaseProgramSettingsViewController> settingViewController = new ArrayList<>();

	@Getter(AccessLevel.NONE)
	private Settings settings;

	private Stage stage;

	private final ErrorAlertBuilder errorAlertBuilder;

	@InjectConstructor
	public ProgramSettingsViewController(FluentClient client, ErrorAlertBuilder errorAlertBuilder)
	{
		this.client = client;
		this.errorAlertBuilder = errorAlertBuilder;
	}

	@Override
	protected void init()
	{
		boxCategories.getStyleClass().add("settings-category-box");

		final SettingsCategory categoryGeneral = createSettingsCategory(ProgramSettingsGeneralViewController.class, Strings.UI_SETTINGS_PROGRAM_GENERAL_TITLE, FontAwesomeType.GEAR_SOLID);

		initButtons();

		selectCategory(categoryGeneral);
	}

	private SettingsCategory createSettingsCategory(Class<? extends BaseProgramSettingsViewController> controllerClass, String localizationKey, FontAwesomeType icon)
	{
		final BaseProgramSettingsViewController viewController = AppContextHolder.getInstance().get(controllerClass);
		settingViewController.add(viewController);

		final SettingsCategory category = new SettingsCategory(Localization.getString(localizationKey), icon, viewController);
		category.setOnAction(this::onSelectCategory);
		boxCategories.getChildren().add(category);
		return category;
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);

		this.stage = stage;

		stage.setResizable(true);

		stage.setWidth(850);
		stage.setHeight(500);

		stage.setMinWidth(850);
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
		this.settings = param.settings;

		settingViewController.forEach(controller -> controller.initParameter(new BaseProgramSettingsViewController.Param(settings)));

		stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PROGRAM_TITLE));
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
		settingViewController.forEach(controller -> controller.applySettings(new BaseProgramSettingsViewController.Param(settings)));

		try
		{
			client.settings().update(settings);
			getStageContainer().ifPresent(NVCStage::close);
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e.getMessage());
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PROGRAM_SETTINGS_SAVE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event)
	{
		settingViewController.forEach(BaseSettingsViewController::cleanup);

		getStageContainer().ifPresent(NVCStage::close);
	}
}
