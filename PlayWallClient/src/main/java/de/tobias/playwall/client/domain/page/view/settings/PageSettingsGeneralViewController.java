package de.tobias.playwall.client.domain.page.view.settings;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.page.PageSettings;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.ColorButton;
import de.tobias.playwall.client.view.components.ColorPicker;
import de.tobias.playwall.client.view.components.ValidatedTextField;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.client.view.validation.Validators;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Viewcontroller for the general page in the page settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/page", view = "PageSettingsGeneralPageView", applyToStage = false)
class PageSettingsGeneralViewController extends BasePageSettingsViewController
{
	@FXML
	private ValidatedTextField textFieldName;

	@FXML
	private ColorButton buttonColor;
	private ColorPicker<ModernColor> colorPicker;

	@InjectConstructor
	public PageSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		final PageSettings pageSettings = param.getPage().getSettings();

		textFieldName.setText(pageSettings.getName());

		final ModernColor color = pageSettings.getColor();
		colorPicker = new ColorPicker<>(color, ModernColor.values(), newColor -> buttonColor.updateColor(newColor));
		buttonColor.updateColor(color);

		textFieldName.setValidator(Validators.notEmpty(Localization.getString(Strings.UI_PAGE_RENAME_ERROR_EMPTY)));

		this.isValidProperty.bind(textFieldName.validProperty());
	}

	@Override
	public void applySettings(Param param)
	{
		final PageSettings pageSettings = param.getPage().getSettings();

		pageSettings.setName(textFieldName.getText());
		pageSettings.setColor(colorPicker.getSelectedColor());
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}


	@FXML
	public void onButtonColor(ActionEvent event)
	{
		colorPicker.hide();
		colorPicker.show(buttonColor);
	}
}
