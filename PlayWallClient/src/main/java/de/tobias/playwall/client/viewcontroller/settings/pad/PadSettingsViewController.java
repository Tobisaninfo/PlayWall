package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import de.tobias.playwall.client.view.components.settings.SettingsCategory;
import de.tobias.playwall.client.viewcontroller.ParamDialogBase;
import de.tobias.playwall.client.viewcontroller.settings.BaseSettingsViewController;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Viewcontroller for the pad settings dialog.
 * Holds the sidebar consisting of {@link de.tobias.playwall.client.view.components.settings.SettingsCategory} instances
 * and a container for showing the currently selected {@link de.tobias.playwall.client.view.components.settings.SettingsPage}
 * as well as the cancel and submit button.
 * On save all settings from all pages will be applied.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/pad", view = "PadSettingsView")
public class PadSettingsViewController extends ParamDialogBase<PadSettingsViewController.Param>
{
	@AllArgsConstructor
	public static class Param
	{
		private Pad pad;
	}

	@FXML
	private VBox boxCategories;

	@FXML
	private VBox settingsPageContainer;

	@FXML
	private HBox boxButtons;

	@Getter(AccessLevel.NONE)
	private final FluentClient client;

	private final List<BasePadSettingsViewController> settingViewController = new ArrayList<>();

	@Getter(AccessLevel.NONE)
	private Pad pad;

	private Stage stage;

	@InjectConstructor
	public PadSettingsViewController(FluentClient client)
	{
		this.client = client;
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

		boxCategories.getStyleClass().add("settings-category-box");

		final PadSettingsGeneralViewController padSettingsGeneralViewController = AppContextHolder.getInstance().get(PadSettingsGeneralViewController.class);
		settingViewController.add(padSettingsGeneralViewController);

		final SettingsCategory categoryGeneral = new SettingsCategory("Allgemein", FontAwesomeType.GEAR_SOLID, padSettingsGeneralViewController);
		categoryGeneral.setOnAction(this::onSelectCategory);
		boxCategories.getChildren().add(categoryGeneral);

		initButtons();

		selectCategory(categoryGeneral);
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
		this.pad = param.pad;

		settingViewController.forEach(controller -> controller.initParameter(new BasePadSettingsViewController.Param(pad)));

		if(pad.getName() == null || pad.getName().isEmpty())
		{
			stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_TITLE_SHORT, pad.getReadablePosition()));
		}
		else
		{
			stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_TITLE, pad.getReadablePosition(), pad.getName()));
		}
	}

	private void initButtons()
	{
		final Button buttonDelete = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_DELETE), FontAwesomeType.TRASH_CAN_SOLID);
		buttonDelete.setId("deleteButton");
		buttonDelete.getStyleClass().add(ViewConstants.DANGER_STYLECLASS);
//		buttonDelete.setOnAction(this::deleteButtonHandler);

		final Button saveButton = new PlayWallButton(Localization.getString("ui.settings.button.save"), FontAwesomeType.FLOPPY_DISK_SOLID);
		saveButton.setId("saveButton");
		saveButton.setDefaultButton(true);
		saveButton.setOnAction(this::saveButtonHandler);

		final Button cancelButton = new PlayWallButton(Localization.getString("ui.settings.button.cancel"), FontAwesomeType.XMARK_SOLID);
		cancelButton.setId("cancelButton");
		cancelButton.setOnAction(this::cancelButtonHandler);

		final Region spacer = new Region();
		boxButtons.getChildren().addAll(buttonDelete, spacer, cancelButton, saveButton);
		HBox.setHgrow(spacer, Priority.ALWAYS);

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
		settingViewController.forEach(controller -> controller.applySettings(new BasePadSettingsViewController.Param(pad)));

		try
		{
			client.pad(pad.getId()).updateSettings(pad);
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
		getStageContainer().ifPresent(NVCStage::close);
	}
}
