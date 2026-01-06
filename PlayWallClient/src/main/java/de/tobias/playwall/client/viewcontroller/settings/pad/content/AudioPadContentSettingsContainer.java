package de.tobias.playwall.client.viewcontroller.settings.pad.content;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.model.project.AudioPadContent;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import de.tobias.playwall.client.viewcontroller.FileChooserWrapper;
import de.tobias.playwall.client.viewcontroller.settings.pad.BasePadSettingsViewController;
import de.tobias.playwall.common.utils.FileFormats;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static de.thecodelabs.utils.util.Localization.getString;

/**
 * Settings related to {@link AudioPadContent}
 */
public class AudioPadContentSettingsContainer extends BasePadContentSettingsContainer<AudioPadContent>
{
	private CheckBox checkboxPlaybackLoop;
	private Slider volumeSlider;

	private final FluentClient fluentClient;

	private final double initialVolume;

	public AudioPadContentSettingsContainer(AudioPadContent padContent, UUID padId, FluentClient fluentClient)
	{
		super(padContent, padId);
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
		buttonChooseFile.setOnAction(this::onButtonFileChooser);

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

		volumeSlider = new Slider(0, 100, 1);
		volumeSlider.setShowTickLabels(true);
		volumeSlider.setShowTickMarks(true);
		volumeSlider.setSnapToTicks(true);
		volumeSlider.setPrefWidth(250);

		volumeSlider.styleProperty().bind(Bindings.createStringBinding(() -> {
			double min = volumeSlider.getMin();
			double max = volumeSlider.getMax();
			double value = volumeSlider.getValue();

			return createSliderStyle(min, max, value);
		}, volumeSlider.valueProperty()));

		volumeSlider.valueProperty().addListener((_, _, newValue) -> {
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

	private void onButtonFileChooser(ActionEvent event)
	{
		final Window owner = ((Node) event.getTarget()).getScene().getWindow();

		final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(getString(Strings.UI_DIALOG_SETTINGS_PAD_OVERRIDE_TITLE));
		alert.setContentText(getString(Strings.UI_DIALOG_SETTINGS_PAD_OVERRIDE_CONTENT));
		alert.initOwner(owner);
		alert.initModality(Modality.WINDOW_MODAL);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(_ ->
		{
			final FileChooserWrapper fileChooser = AppContextHolder.getInstance().get(FileChooserWrapper.class);
			fileChooser.setExtensionFilter(FileFormats.FILE_FORMATS.stream().map(format ->
					new FileChooser.ExtensionFilter(
							Localization.getString("FileFormat." + format.contentType().name()),
							format.extensions().stream().map(ext -> "*." + ext).toList()
					)).toList());
			final Optional<Path> path = fileChooser.showOpenFile(owner);

			if(path.isPresent())
			{
				cleanup();

				try
				{
					fluentClient.pad(padId).newMedia(path.get());
					final Stage stage = (Stage) ((Node) event.getTarget()).getScene().getWindow();
					stage.close();
				}
				catch(PlayWallApiException ex)
				{
					// TODO: error handling
					throw new RuntimeException(ex);
				}
			}
		});
	}

	private void onButtonShowInFolder(ActionEvent event)
	{
		NativeApplication.sharedInstance().showFileInFileViewer(Paths.get(padContent.getMediaPath()));
	}

	private String createSliderStyle(double min, double max, double value)
	{
		final double percentage = 100.0 * (value - min) / (max - min);

		final MessageFormat messageFormat = new MessageFormat(
				"-slider-track-color: linear-gradient(to right, " +
						"{0} 0%, " +
						"{0} {2}%, " +
						"{1} {2}%, " +
						"{1} {3}%, " +
						"{0} {3}%, " +
						"{0} 100%);", Locale.ENGLISH);

		final Object[] arguments = {ViewConstants.SLIDER_DEFAULT_BACKGROUND_COLOR, ViewConstants.PRIMARY_COLOR, min, percentage};
		return messageFormat.format(arguments, new StringBuffer(), null).toString();
	}
}
