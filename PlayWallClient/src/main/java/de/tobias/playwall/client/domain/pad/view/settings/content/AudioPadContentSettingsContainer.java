package de.tobias.playwall.client.domain.pad.view.settings.content;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.pad.AudioPadContent;
import de.tobias.playwall.client.domain.pad.view.settings.BasePadSettingsViewController;
import de.tobias.playwall.client.domain.pad.view.settings.PadSettingsViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import de.tobias.playwall.client.view.components.VolumeSlider;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.nio.file.Paths;
import java.util.UUID;

/**
 * Settings related to {@link AudioPadContent}
 */
public class AudioPadContentSettingsContainer extends BasePadContentSettingsContainer<AudioPadContent>
{
	private CheckBox checkboxPlaybackLoop;
	private VolumeSlider volumeSlider;

	private final FluentClient fluentClient;

	private final double initialVolume;

	public AudioPadContentSettingsContainer(AudioPadContent padContent, UUID padId, FluentClient fluentClient, PadSettingsViewController parentDialog)
	{
		super(padContent, padId, parentDialog);
		this.fluentClient = fluentClient;

		this.initialVolume = padContent.getVolume();

		getChildren().addAll(createFileSettings(), new Separator());
		getChildren().addAll(createPlaybackSettings(), new Separator());
		getChildren().addAll(createVolumeSettings(), new Separator());

		isValidProperty.set(true);
	}

	private SettingsRow createFileSettings()
	{
		Label labelFilePath = new Label(padContent.getMediaPath());
		labelFilePath.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
		labelFilePath.setEllipsisString(" ... ");
		final Tooltip tooltip = new Tooltip();
		tooltip.setText(padContent.getMediaPath());
		tooltip.setShowDelay(Duration.millis(300));
		labelFilePath.setTooltip(tooltip);

		final PlayWallButton buttonChooseFile = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_CHOOSE_PATH), FontAwesomeType.FOLDER_OPEN_SOLID);
		buttonChooseFile.setId("buttonChooseFile");
		buttonChooseFile.setOnAction(parentDialog::onButtonFileChooser);

		final PlayWallButton buttonShowInFolder = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_SHOW_IN_FOLDER), FontAwesomeType.FOLDER_SOLID);
		buttonShowInFolder.setOnAction(this::onButtonShowInFolder);

		final HBox boxButtons = new HBox(ViewConstants.DEFAULT_SPACING);
		boxButtons.setAlignment(Pos.CENTER_LEFT);
		boxButtons.getChildren().addAll(buttonChooseFile, buttonShowInFolder);

		final SettingsRow settingsRowFile = new SettingsRow();
		settingsRowFile.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_FILE));
		settingsRowFile.setIcon(FontAwesomeType.FILE_SOLID);
		settingsRowFile.add(labelFilePath, 1, 0);
		settingsRowFile.add(boxButtons, 1, 1);
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

	private SettingsRow createVolumeSettings()
	{
		final SettingsRow settingsRowVolume = new SettingsRow();
		settingsRowVolume.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_VOLUME));
		settingsRowVolume.setIcon(FontAwesomeType.VOLUME_HIGH_SOLID);

		volumeSlider = new VolumeSlider(VolumeSlider.BOOSTED_VALUE);
		volumeSlider.valueProperty().addListener((_, oldValue, newValue) -> {
			if(Math.abs(oldValue.doubleValue() - newValue.doubleValue()) < VolumeSlider.UPDATE_THRESHOLD)
			{
				return;
			}

			try
			{
				fluentClient.pad(padId).changeVolume(newValue.doubleValue() / 100.0);
			}
			catch(PlayWallApiException e)
			{
				Logger.error(e.getMessage());
			}
		});

		volumeSlider.setValue(padContent.getVolume() * 100);
		settingsRowVolume.add(volumeSlider, 1, 0);

		return settingsRowVolume;
	}

	@Override
	public void applySettings(BasePadSettingsViewController.Param param)
	{
		if(param.getPad().getContent() instanceof AudioPadContent audioPadContent)
		{
			audioPadContent.setLoop(checkboxPlaybackLoop.isSelected());
			audioPadContent.setVolume(volumeSlider.getValue() / 100.0);
		}
	}

	@Override
	public void cleanup()
	{
		try
		{
			fluentClient.pad(padId).changeVolume(initialVolume);
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e.getMessage());
		}
	}

	private void onButtonShowInFolder(ActionEvent event)
	{
		NativeApplication.sharedInstance().showFileInFileViewer(Paths.get(padContent.getMediaPath()));
	}
}
