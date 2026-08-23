package de.tobias.playwall.client.domain.pad.view.settings;

import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.pad.view.settings.content.PadContentSettingsContainerFactory;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.ColorButton;
import de.tobias.playwall.client.view.components.ColorPicker;
import de.tobias.playwall.client.view.components.EnumCell;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

/**
 * Viewcontroller for the view page in the pad settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/pad", view = "PadSettingsDisplayPageView", applyToStage = false)
public class PadSettingsDisplayViewController extends BasePadSettingsViewController
{
	@FXML
	private CheckBox checkboxColorDefault;

	@FXML
	private ColorButton buttonColorDefault;

	@FXML
	private CheckBox checkboxColorPlay;

	@FXML
	private ColorButton buttonColorPlay;

	@FXML
	private CheckBox checkboxColorIntro;

	@FXML
	private ColorButton buttonColorIntro;

	@FXML
	private ComboBox<TimeMode> comboBoxTime;

	private ColorPicker<ModernColor> colorPickerDefault;
	private ColorPicker<ModernColor> colorPickerPlay;
	private ColorPicker<ModernColor> colorPickerIntro;

	@InjectConstructor
	public PadSettingsDisplayViewController(FluentClient client, PadContentSettingsContainerFactory padContentSettingsContainerFactory)
	{
		super(client);
	}

	@Override
	protected void init()
	{
		comboBoxTime.getItems().add(null);  // use project settings
		comboBoxTime.getItems().addAll(TimeMode.values());
		comboBoxTime.setButtonCell(new EnumCell<>(Strings.UI_SETTINGS_PROJECT_TIME_MODE_BASE));
		comboBoxTime.setCellFactory(_ -> new EnumCell<>(Strings.UI_SETTINGS_PROJECT_TIME_MODE_BASE));
	}

	@Override
	public void initParameter(Param param)
	{
		colorPickerDefault = initColorPicker(param.getPad().getDefaultColor(), buttonColorDefault, checkboxColorDefault);
		colorPickerPlay = initColorPicker(param.getPad().getPlayColor(), buttonColorPlay, checkboxColorPlay);
		colorPickerIntro = initColorPicker(param.getPad().getIntroColor(), buttonColorIntro, checkboxColorIntro);

		comboBoxTime.getSelectionModel().select(param.pad.getTimeMode());

		this.isValidProperty.set(true);
	}

	private ColorPicker<ModernColor> initColorPicker(ModernColor color, ColorButton buttonColor, CheckBox checkboxColor)
	{
		ModernColor actualColor = color;
		if(actualColor == null)
		{
			actualColor = ModernColor.GRAY1;
		}

		final boolean isOverrideActive = color != null;

		final ColorPicker<ModernColor> colorPicker = new ColorPicker<>(actualColor, ModernColor.values(), buttonColor::updateColor);
		buttonColor.updateColor(actualColor);
		buttonColor.setDisable(!isOverrideActive);
		checkboxColor.setSelected(isOverrideActive);
		checkboxColor.selectedProperty().addListener((_, _, newValue) -> buttonColor.setDisable(!newValue));

		return colorPicker;
	}

	@Override
	public void applySettings(Param param)
	{
		param.pad.setTimeMode(comboBoxTime.getSelectionModel().getSelectedItem());

		ModernColor defaultColor = null;
		if(checkboxColorDefault.isSelected())
		{
			defaultColor = colorPickerDefault.getSelectedColor();
		}
		param.pad.setDefaultColor(defaultColor);

		ModernColor playColor = null;
		if(checkboxColorPlay.isSelected())
		{
			playColor = colorPickerPlay.getSelectedColor();
		}
		param.pad.setPlayColor(playColor);

		ModernColor introColor = null;
		if(checkboxColorIntro.isSelected())
		{
			introColor = colorPickerIntro.getSelectedColor();
		}
		param.pad.setIntroColor(introColor);
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
