package de.tobias.playwall.client.domain.page.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.net.FluentClient;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Viewcontroller for the general page in the page settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/page", view = "PageSettingsGeneralPageView", applyToStage = false)
class PageSettingsGeneralViewController extends BasePageSettingsViewController
{
	@FXML
	private TextField textFieldName;

	@InjectConstructor
	public PageSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		textFieldName.setText(param.getPage().getName());

		// TODO Validate duplicate page names
		/*
			if(newValue.isEmpty())
			{
				return Localization.getString(Strings.UI_PAGE_RENAME_ERROR_EMPTY);
			}
			final List<String> usedPageNames = projectController.getProject().getPages().stream()
					.map(Page::getName)
					.toList();
			return usedPageNames.contains(newValue) ? Localization.getString(Strings.UI_PAGE_RENAME_ERROR_DUPLICATE) : null;
		 */

		this.isValidProperty.bind(textFieldName.textProperty().isNotEmpty());
	}

	@Override
	public void applySettings(Param param)
	{
		final Page page = param.getPage();

		page.setName(textFieldName.getText());
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}
}
