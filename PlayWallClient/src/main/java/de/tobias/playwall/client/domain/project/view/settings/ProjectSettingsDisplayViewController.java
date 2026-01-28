package de.tobias.playwall.client.domain.project.view.settings;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.ColorPicker;
import de.tobias.playwall.client.view.components.EnumCell;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

/**
 * Viewcontroller for the view page in the project settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsDisplayPageView", applyToStage = false)
public class ProjectSettingsDisplayViewController extends BaseProjectSettingsViewController
{
	@FXML
	private Button buttonColorDefault;

	@FXML
	private Button buttonColorPlay;

	@FXML
	private ComboBox<TimeMode> comboBoxTime;

	private ColorPicker colorPickerDefault;
	private ColorPicker colorPickerPlay;

	@InjectConstructor
	public ProjectSettingsDisplayViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	protected void init()
	{
		initColorButton(buttonColorDefault);
		initColorButton(buttonColorPlay);

		comboBoxTime.getItems().addAll(TimeMode.values());
		comboBoxTime.setButtonCell(new EnumCell<>(Strings.UI_SETTINGS_PROJECT_TIME_MODE_BASE));
		comboBoxTime.setCellFactory(_ -> new EnumCell<>(Strings.UI_SETTINGS_PROJECT_TIME_MODE_BASE));
	}

	private void initColorButton(Button button)
	{
		final FontIcon icon = new FontIcon(FontAwesomeType.CIRCLE_ARROW_DOWN_SOLID);
		icon.setSize(14);
		icon.setMouseTransparent(true);
		button.setGraphic(icon);
		button.setAlignment(Pos.CENTER_RIGHT);
		button.getStyleClass().add("button-color");
	}

	@Override
	public void initParameter(Param param)
	{
		colorPickerDefault = new ColorPicker(param.getProjectMetadata().getDefaultColor(), ModernColor.values(), (newColor) -> buttonColorDefault.setStyle("-fx-background-color: " + newColor.paint()));
		buttonColorDefault.setStyle("-fx-background-color: " + param.getProjectMetadata().getDefaultColor().paint());
		colorPickerPlay = new ColorPicker(param.getProjectMetadata().getPlayColor(), ModernColor.values(), (newColor) -> buttonColorPlay.setStyle("-fx-background-color: " + newColor.paint()));
		buttonColorPlay.setStyle("-fx-background-color: " + param.getProjectMetadata().getPlayColor().paint());

		comboBoxTime.getSelectionModel().select(param.projectMetadata.getTimeMode());

		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
		param.getProjectMetadata().setTimeMode(comboBoxTime.getSelectionModel().getSelectedItem());
		param.getProjectMetadata().setDefaultColor(colorPickerDefault.getSelectedColor());
		param.getProjectMetadata().setPlayColor(colorPickerPlay.getSelectedColor());
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}

	@FXML
	public void onButtonColorDefault(ActionEvent event)
	{
		colorPickerDefault.hide();
		colorPickerDefault.show(buttonColorDefault);
	}

	@FXML
	public void onButtonColorPlay(ActionEvent event)
	{
		colorPickerPlay.hide();
		colorPickerPlay.show(buttonColorPlay);
	}
}
