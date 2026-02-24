package de.tobias.playwall.client.domain.pad.view.settings;

import de.thecodelabs.logger.Logger;
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
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Window;
import lombok.AccessLevel;
import lombok.Getter;

import static de.thecodelabs.utils.util.Localization.getString;

/**
 * Viewcontroller for the pad settings dialog.
 * Holds the sidebar consisting of {@link de.tobias.playwall.client.view.components.settings.SettingsCategory} instances
 * and a container for showing the currently selected {@link de.tobias.playwall.client.view.components.settings.SettingsPage}
 * as well as the cancel and submit button.
 * On save all settings from all pages will be applied.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/pad", view = "PadSettingsView")
public class PadSettingsViewController extends BaseSettingsDialogController<BasePadSettingsViewController.Param>
{
	@FXML
	private PlayWallButton deleteButton;

	@Getter(AccessLevel.NONE)
	private Pad pad;

	@InjectConstructor
	public PadSettingsViewController(FluentClient client, ErrorAlertBuilder errorAlertBuilder)
	{
		super(client, errorAlertBuilder);
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

	@Override
	public void initParameter(BasePadSettingsViewController.Param param)
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

		deleteButton.setVisible(pad.getContent() != null);
	}

	@FXML
	protected void saveButtonHandler(ActionEvent event)
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
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAD_SETTINGS_SAVE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	@FXML
	public void deleteButtonHandler(ActionEvent event)
	{
		try
		{
			client.pad(pad.getId()).delete();
			getStageContainer().ifPresent(NVCStage::close);
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e.getMessage());
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAD_DELETE), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
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
				errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAD_LOAD), ex.getMessage(), ex.getError(), getContainingWindow()).showAndWait();
			}
		});
	}
}
