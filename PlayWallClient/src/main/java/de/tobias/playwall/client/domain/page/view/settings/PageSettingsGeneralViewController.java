package de.tobias.playwall.client.domain.page.view.settings;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.PageSettings;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.ColorButton;
import de.tobias.playwall.client.view.components.ColorPicker;
import de.tobias.playwall.client.view.components.ValidatedTextField;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.client.view.validation.Validators;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.util.List;

/**
 * Viewcontroller for the general page in the page settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/page", view = "PageSettingsGeneralPageView", applyToStage = false)
class PageSettingsGeneralViewController extends BasePageSettingsViewController
{
	private final ClientProjectController projectController;

	@FXML
	private ValidatedTextField textFieldName;

	@FXML
	private ColorButton buttonColor;
	private ColorPicker colorPicker;

	@InjectConstructor
	public PageSettingsGeneralViewController(FluentClient client, ClientProjectController projectController)
	{
		super(client);
		this.projectController = projectController;
	}

	@Override
	public void initParameter(Param param)
	{
		final PageSettings pageSettings = param.getPage().getSettings();

		textFieldName.setText(pageSettings.getName());

		final ModernColor color = pageSettings.getColor();
		colorPicker = new ColorPicker(color, ModernColor.values(), newColor -> buttonColor.updateColor(newColor));
		buttonColor.updateColor(color);

		final List<String> otherPageNames = projectController.getProject().getPages().stream()
				.filter(page -> !param.getPage().equals(page))
				.map(Page::getSettings)
				.map(PageSettings::getName)
				.toList();
		textFieldName.setValidator(Validators.notEmpty(Localization.getString(Strings.UI_PAGE_RENAME_ERROR_EMPTY)).
				and(input -> otherPageNames.contains(input) ? Localization.getString(Strings.UI_PAGE_RENAME_ERROR_DUPLICATE) : null)
		);

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
