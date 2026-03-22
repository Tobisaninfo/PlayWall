package de.tobias.playwall.client.domain.project.view.settings;

import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.ColorButton;
import de.tobias.playwall.client.view.components.ColorPicker;
import de.tobias.playwall.client.view.components.EnumCell;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

/**
 * Viewcontroller for the view page in the project settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsDisplayPageView", applyToStage = false)
public class ProjectSettingsDisplayViewController extends BaseProjectSettingsViewController
{
	@FXML
	private ColorButton buttonColorDefault;

	@FXML
	private ColorButton buttonColorPlay;

	@FXML
	private ColorButton buttonColorIntro;

	@FXML
	private ComboBox<TimeMode> comboBoxTime;

	private ColorPicker colorPickerDefault;
	private ColorPicker colorPickerPlay;
	private ColorPicker colorPickerIntro;

	@InjectConstructor
	public ProjectSettingsDisplayViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	protected void init()
	{
		comboBoxTime.getItems().addAll(TimeMode.values());
		comboBoxTime.setButtonCell(new EnumCell<>(Strings.UI_SETTINGS_PROJECT_TIME_MODE_BASE));
		comboBoxTime.setCellFactory(_ -> new EnumCell<>(Strings.UI_SETTINGS_PROJECT_TIME_MODE_BASE));
	}

	@Override
	public void initParameter(Param param)
	{
		final ModernColor defaultColor = param.getProjectMetadata().getDefaultColor();
		colorPickerDefault = new ColorPicker(defaultColor, ModernColor.values(), newColor -> buttonColorDefault.updateColor(newColor));
		buttonColorDefault.updateColor(defaultColor);

		final ModernColor playColor = param.getProjectMetadata().getPlayColor();
		colorPickerPlay = new ColorPicker(playColor, ModernColor.values(), newColor -> buttonColorPlay.updateColor(newColor));
		buttonColorPlay.updateColor(playColor);

		final ModernColor introColor = param.getProjectMetadata().getIntroColor();
		colorPickerIntro = new ColorPicker(introColor, ModernColor.values(), newColor -> buttonColorIntro.updateColor(newColor));
		buttonColorIntro.updateColor(introColor);

		comboBoxTime.getSelectionModel().select(param.projectMetadata.getTimeMode());

		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
		param.getProjectMetadata().setTimeMode(comboBoxTime.getSelectionModel().getSelectedItem());
		param.getProjectMetadata().setDefaultColor(colorPickerDefault.getSelectedColor());
		param.getProjectMetadata().setPlayColor(colorPickerPlay.getSelectedColor());
		param.getProjectMetadata().setIntroColor(colorPickerIntro.getSelectedColor());
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

	@FXML
	public void onButtonColorIntro(ActionEvent event)
	{
		colorPickerIntro.hide();
		colorPickerIntro.show(buttonColorIntro);
	}
}
