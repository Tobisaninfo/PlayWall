package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.model.project.PadContent;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.viewcontroller.settings.pad.content.BasePadContentSettingsContainer;
import de.tobias.playwall.client.viewcontroller.settings.pad.content.PadContentSettingsContainerFactory;
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

	private final PadContentSettingsContainerFactory padContentSettingsContainerFactory;

	@InjectConstructor
	public PadSettingsGeneralViewController(FluentClient client, PadContentSettingsContainerFactory padContentSettingsContainerFactory)
	{
		super(client);
		this.padContentSettingsContainerFactory = padContentSettingsContainerFactory;
	}

	@Override
	public void initParameter(Param param)
	{
		textFieldName.setText(param.pad.getName());

		// dynamic content based on pad content

		if(padContentSettingsContainer != null)
		{
			settingsPage.getItems().remove(padContentSettingsContainer);
			padContentSettingsContainer = null;
		}

		padContentSettingsContainer = padContentSettingsContainerFactory.createPadContentSettingsContainer(param.pad, param.parentDialog);
		settingsPage.getItems().add(padContentSettingsContainer);

		if(param.getPad().getContent() != null)
		{
			this.isValidProperty.bind(textFieldName.textProperty().isNotEmpty().and(padContentSettingsContainer.getIsValidProperty()));
		}
		else
		{
			this.isValidProperty.unbind();
			this.isValidProperty.set(true);
		}
	}

	@Override
	public void applySettings(Param param)
	{
		param.pad.setName(textFieldName.getText());

		if(padContentSettingsContainer != null)
		{
			padContentSettingsContainer.applySettings(param);
		}
	}

	@Override
	public void cleanup()
	{
		if(padContentSettingsContainer != null)
		{
			padContentSettingsContainer.cleanup();
		}
	}
}
