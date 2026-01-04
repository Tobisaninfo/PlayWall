package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.model.project.AudioPadContent;
import de.tobias.playwall.client.model.project.PadContent;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.settings.SettingsPage;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

@ViewController(path = "de/tobias/playwall/client/view/settings/pad", view = "PadSettingsGeneralPageView", applyToStage = false)
public class PadSettingsGeneralViewController extends BasePadSettingsViewController
{
	@FXML
	private SettingsPage settingsPage;

	@FXML
	private TextField textFieldName;

	private AudioPadContentSettingsContainer audioPadContentSettingsContainer;

	@InjectConstructor
	public PadSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		textFieldName.setText(param.pad.getName());

		settingsPage.getItems().removeIf(n -> n instanceof BasePadContentSettingsContainer);

		final PadContent content = param.pad.getContent();
		if(content != null)
		{
			switch(content)
			{
				case AudioPadContent audioPadContent ->
				{
					audioPadContentSettingsContainer = new AudioPadContentSettingsContainer();
					audioPadContentSettingsContainer.init(audioPadContent);
					settingsPage.getItems().add(audioPadContentSettingsContainer);
				}
			}
		}

		this.isValidProperty.bind(textFieldName.textProperty().isNotEmpty());
	}

	@Override
	public void applySettings(Param param)
	{
		param.pad.setName(textFieldName.getText());

		final PadContent content = param.pad.getContent();
		if(content != null)
		{
			switch(content)
			{
				case AudioPadContent audioPadContent -> audioPadContentSettingsContainer.applySettings(audioPadContent);
			}
		}
	}
}
