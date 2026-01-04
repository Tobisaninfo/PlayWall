package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.model.project.AudioPadContent;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;

public class AudioPadContentSettingsContainer extends BasePadContentSettingsContainer<AudioPadContent>
{
	private final CheckBox checkboxPlaybackLoop;

	public AudioPadContentSettingsContainer()
	{
		super();

		final SettingsRow settingsRowPlayback = new SettingsRow();
		settingsRowPlayback.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_PLAYBACK));
		settingsRowPlayback.setIcon(FontAwesomeType.PLAY_SOLID);

		checkboxPlaybackLoop = new CheckBox(Localization.getString(Strings.UI_SETTINGS_PAD_PLAYBACK_LOOP));
		settingsRowPlayback.add(checkboxPlaybackLoop, 1, 0);

		getChildren().addAll(settingsRowPlayback, new Separator());
	}

	@Override
	protected void init(AudioPadContent padContent)
	{
		checkboxPlaybackLoop.setSelected(padContent.isLoop());
	}

	@Override
	public void applySettings(AudioPadContent padContent)
	{
		padContent.setLoop(checkboxPlaybackLoop.isSelected());
	}
}
