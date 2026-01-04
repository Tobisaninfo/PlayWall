package de.tobias.playwall.client.viewcontroller.settings.pad.content;

import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.model.project.AudioPadContent;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.Paths;

/**
 * Settings related to {@link AudioPadContent}
 */
public class AudioPadContentSettingsContainer extends BasePadContentSettingsContainer<AudioPadContent>
{
	private CheckBox checkboxPlaybackLoop;

	public AudioPadContentSettingsContainer(AudioPadContent padContent)
	{
		super(padContent);

		getChildren().addAll(createFileSettings(), new Separator());
		getChildren().addAll(createPlaybackSettings(), new Separator());

		isValidProperty.set(true);
	}

	private SettingsRow createFileSettings()
	{
		final Label labelFilePath = new Label(padContent.getMediaPath());
		final PlayWallButton buttonChooseFile = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_CHOOSE_PATH), FontAwesomeType.FOLDER_OPEN_SOLID);
		buttonChooseFile.setMinWidth(120);

		final PlayWallButton buttonShowInFolder = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_SHOW_IN_FOLDER), FontAwesomeType.FOLDER_SOLID);
		buttonShowInFolder.setOnAction(this::onButtonShowInFolder);

		final PlayWallButton buttonDelete = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_DELETE), FontAwesomeType.TRASH_CAN_SOLID);

		final HBox boxFile = new HBox(ViewConstants.DEFAULT_SPACING);
		boxFile.setAlignment(Pos.CENTER_LEFT);
		boxFile.getChildren().addAll(labelFilePath, buttonChooseFile);

		final HBox boxButtons = new HBox(ViewConstants.DEFAULT_SPACING);
		boxButtons.setAlignment(Pos.CENTER_LEFT);
		boxButtons.getChildren().addAll(buttonShowInFolder, buttonDelete);

		final VBox boxFileSettings = new VBox(ViewConstants.DEFAULT_SPACING);
		boxFileSettings.setAlignment(Pos.TOP_LEFT);
		boxFileSettings.getChildren().addAll(boxFile, boxButtons);

		final SettingsRow settingsRowFile = new SettingsRow();
		settingsRowFile.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_FILE));
		settingsRowFile.setIcon(FontAwesomeType.FILE_SOLID);
		settingsRowFile.add(boxFileSettings, 1, 0);
		return settingsRowFile;
	}

	private SettingsRow createPlaybackSettings()
	{
		final SettingsRow settingsRowPlayback = new SettingsRow();
		settingsRowPlayback.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_PLAYBACK));
		settingsRowPlayback.setIcon(FontAwesomeType.PLAY_SOLID);

		checkboxPlaybackLoop = new CheckBox(Localization.getString(Strings.UI_SETTINGS_PAD_PLAYBACK_LOOP));
		checkboxPlaybackLoop.setSelected(padContent.isLoop());
		settingsRowPlayback.add(checkboxPlaybackLoop, 1, 0);

		return settingsRowPlayback;
	}

	@Override
	public void applySettings()
	{
		padContent.setLoop(checkboxPlaybackLoop.isSelected());
	}

	private void onButtonShowInFolder(ActionEvent event)
	{
		NativeApplication.sharedInstance().showFileInFileViewer(Paths.get(padContent.getMediaPath()));
	}
}
