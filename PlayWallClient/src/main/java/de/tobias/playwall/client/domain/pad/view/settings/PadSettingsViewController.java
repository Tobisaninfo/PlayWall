package de.tobias.playwall.client.domain.pad.view.settings;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.ParamDialogBase;
import de.tobias.playwall.client.view.components.ErrorAlert;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.PseudoClasses;
import de.tobias.playwall.client.view.components.ViewConstants;
import de.tobias.playwall.client.view.components.settings.SettingsCategory;
import de.tobias.playwall.client.view.settings.BaseSettingsViewController;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static de.thecodelabs.utils.util.Localization.getString;

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

	private Button buttonDelete;

	@InjectConstructor
	public PadSettingsViewController(FluentClient client)
	{
		this.client = client;
	}

	@Override
	protected void init()
	{
		boxCategories.getStyleClass().add("settings-category-box");

		final SettingsCategory categoryGeneral = createSettingsCategory(PadSettingsGeneralViewController.class, Strings.UI_SETTINGS_PAD_GENERAL_TITLE, FontAwesomeType.GEAR_SOLID);
		createSettingsCategory(PadSettingsDisplayViewController.class, Strings.UI_SETTINGS_PROJECT_VIEW_TITLE, FontAwesomeType.IMAGE_SOLID);

		initButtons();

		selectCategory(categoryGeneral);
	}

	private SettingsCategory createSettingsCategory(Class<? extends BasePadSettingsViewController> controllerClass, String localizationKey, FontAwesomeType icon)
	{
		final BasePadSettingsViewController viewController = AppContextHolder.getInstance().get(controllerClass);
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
		boxCategories.getChildren().forEach(c -> c.pseudoClassStateChanged(PseudoClasses.SELECTED, false));
		boxCategories.getChildren().stream()
				.filter(c -> c.equals(category))
				.findFirst()
				.ifPresent(c -> c.pseudoClassStateChanged(PseudoClasses.SELECTED, true));

		settingsPageContainer.getChildren().setAll(category.getSettingsPageController().getSettingsPage());
	}

	@Override
	public void initParameter(Param param)
	{
		this.pad = param.pad;

		settingViewController.forEach(controller -> controller.initParameter(new BasePadSettingsViewController.Param(pad, this)));

		if(pad.getName() == null || pad.getName().isEmpty())
		{
			stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_TITLE_SHORT, pad.getReadablePosition()));
		}
		else
		{
			stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_TITLE, pad.getReadablePosition(), pad.getName()));
		}

		buttonDelete.setVisible(pad.getContent() != null);
	}

	private void initButtons()
	{
		buttonDelete = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_DELETE), FontAwesomeType.TRASH_CAN_SOLID);
		buttonDelete.setId("deleteButton");
		buttonDelete.getStyleClass().add(ViewConstants.DANGER_STYLECLASS);
		buttonDelete.setOnAction(this::deleteButtonHandler);

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
		settingViewController.forEach(controller -> controller.applySettings(new BasePadSettingsViewController.Param(pad, this)));

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
		settingViewController.forEach(BaseSettingsViewController::cleanup);

		getStageContainer().ifPresent(NVCStage::close);
	}

	@FXML
	private void deleteButtonHandler(ActionEvent event)
	{
		try
		{
			client.pad(pad.getId()).delete();
			getStageContainer().ifPresent(NVCStage::close);
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e.getMessage());
			Alerts.getInstance().createAlert(Alert.AlertType.WARNING, null, e.getMessage(), getContainingWindow()).showAndWait();
		}
	}

	@FXML
	public void onButtonFileChooser(ActionEvent event)
	{
		final Window owner = ((Node) event.getTarget()).getScene().getWindow();

		if(!hasChanges())
		{
			showFileChooser(event);
			return;
		}

		final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(getString(Strings.UI_DIALOG_SETTINGS_PAD_OVERRIDE_TITLE));
		alert.setContentText(getString(Strings.UI_DIALOG_SETTINGS_PAD_OVERRIDE_CONTENT));
		alert.initOwner(owner);
		alert.initModality(Modality.WINDOW_MODAL);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(_ -> showFileChooser(event));
	}

	private boolean hasChanges()
	{
		final Pad padCopy = pad.copy();

		settingViewController.forEach(controller -> controller.applySettings(new BasePadSettingsViewController.Param(padCopy, this)));

		return !padCopy.equals(pad);
	}

	private void showFileChooser(ActionEvent event)
	{
		final FileChooserWrapper fileChooser = AppContextHolder.getInstance().get(FileChooserWrapper.class);
		fileChooser.showByActionEvent(event).ifPresent(path -> {
			settingViewController.forEach(BaseSettingsViewController::cleanup);

			try
			{
				client.pad(pad.getId()).newMedia(path);
				stage.close();
			}
			catch(PlayWallApiException ex)
			{
				Logger.error(ex);
				ErrorAlert.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAD_LOAD), ex.getMessage(), ex.getError(), getContainingWindow()).showAndWait();
			}
		});
	}
}
