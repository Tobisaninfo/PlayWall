package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.model.project.AudioPadContent;
import de.tobias.playwall.client.model.project.PadContent;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.settings.SettingsPage;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;

@ViewController(path = "de/tobias/playwall/client/view/settings/pad", view = "PadSettingsGeneralPageView", applyToStage = false)
public class PadSettingsGeneralViewController extends BasePadSettingsViewController
{
	@FXML
	private SettingsPage settingsPage;

	@FXML
	private TextField textFieldName;

	private SettingsRow settingsRowPlayback;
	private CheckBox checkboxPlaybackLoop;

	@InjectConstructor
	public PadSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(BasePadSettingsViewController.Param param)
	{
		textFieldName.setText(param.pad.getName());
		final PadContent content = param.pad.getContent();
		if(content instanceof AudioPadContent audioPadContent)
		{
			if(settingsRowPlayback == null)
			{
				createPlaybackSettings();
			}

			checkboxPlaybackLoop.setSelected(audioPadContent.isLoop());
		}
		else
		{
			settingsRowPlayback = null;
			checkboxPlaybackLoop = null;
		}

		this.isValidProperty.bind(textFieldName.textProperty().isNotEmpty());
	}

	private void createPlaybackSettings()
	{
		settingsRowPlayback = new SettingsRow();
		settingsRowPlayback.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_PLAYBACK));
		settingsRowPlayback.setIcon(FontAwesomeType.PLAY_SOLID);

		checkboxPlaybackLoop = new CheckBox(Localization.getString(Strings.UI_SETTINGS_PAD_PLAYBACK_LOOP));
		settingsRowPlayback.add(checkboxPlaybackLoop, 1, 0);

		settingsPage.getItems().addAll(settingsRowPlayback, new Separator());
	}

	@Override
	public void applySettings(BasePadSettingsViewController.Param param)
	{
		param.pad.setName(textFieldName.getText());

		final PadContent content = param.pad.getContent();
		if(content instanceof AudioPadContent audioPadContent)
		{
			if(checkboxPlaybackLoop != null)
			{
				audioPadContent.setLoop(checkboxPlaybackLoop.isSelected());
			}
		}
	}
}
