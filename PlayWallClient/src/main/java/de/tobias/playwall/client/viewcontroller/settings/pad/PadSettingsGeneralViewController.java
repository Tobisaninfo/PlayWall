package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.model.project.PadContent;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.settings.SettingsPage;
import de.tobias.playwall.client.viewcontroller.settings.pad.content.BasePadContentSettingsContainer;
import de.tobias.playwall.client.viewcontroller.settings.pad.content.PadContentSettingsContainerFactory;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Viewcontroller for the general page in the pad settings dialog.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/pad", view = "PadSettingsGeneralPageView", applyToStage = false)
public class PadSettingsGeneralViewController extends BasePadSettingsViewController
{
	@FXML
	private SettingsPage settingsPage;

	@FXML
	private TextField textFieldName;

	private BasePadContentSettingsContainer<? extends PadContent> padContentSettingsContainer;

	@InjectConstructor
	public PadSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		textFieldName.setText(param.pad.getName());
		BooleanBinding isValidBinding = textFieldName.textProperty().isNotEmpty();

		// dynamic content based on pad content

		if(padContentSettingsContainer != null)
		{
			settingsPage.getItems().remove(padContentSettingsContainer);
			padContentSettingsContainer = null;
		}

		final PadContent content = param.pad.getContent();
		if(content != null)
		{
			padContentSettingsContainer = PadContentSettingsContainerFactory.createPadContentSettingsContainer(content);
			settingsPage.getItems().add(padContentSettingsContainer);
			isValidBinding = isValidBinding.and(padContentSettingsContainer.getIsValidProperty());
		}

		this.isValidProperty.bind(isValidBinding);
	}

	@Override
	public void applySettings(Param param)
	{
		param.pad.setName(textFieldName.getText());

		if(padContentSettingsContainer != null)
		{
			padContentSettingsContainer.applySettings();
		}
	}
}
