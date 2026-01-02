package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.settings.SettingsCategory;
import de.tobias.playwall.client.view.components.settings.SettingsPage;
import de.tobias.playwall.client.viewcontroller.ParamDialogBase;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
	private SettingsPage settingsPage;

	@FXML
	private TextField textFieldName;

	@Getter(AccessLevel.NONE)
	private final FluentClient client;

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

		stage.setWidth(750);
		stage.setHeight(400);

		stage.setMinWidth(750);
		stage.setMinHeight(400);

		boxCategories.getStyleClass().add("settings-category-box");

		final SettingsCategory categoryGeneral = new SettingsCategory("Allgemein", FontAwesomeType.GEAR_SOLID);
		boxCategories.getChildren().add(categoryGeneral);

		categoryGeneral.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);

//		settingsPage.getSaveButton().disableProperty().bind(textFieldName.textProperty().isEmpty());
	}

	@Override
	public void initParameter(Param param)
	{
		this.pad = param.pad;

		if(pad.getName() == null || pad.getName().isEmpty())
		{
			stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_TITLE_SHORT, pad.getPosition()));
		}
		else
		{
			stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_TITLE, pad.getPosition(), pad.getName()));
		}

		textFieldName.setText(pad.getName());
	}

	@FXML
	private void finishButtonHandler(ActionEvent event)
	{
		final String name = textFieldName.getText();

		try
		{
			client.pad(pad.getId()).updateSettings(name);
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
