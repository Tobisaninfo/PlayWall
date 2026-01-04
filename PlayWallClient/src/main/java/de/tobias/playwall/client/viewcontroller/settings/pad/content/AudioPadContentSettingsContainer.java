package de.tobias.playwall.client.viewcontroller.settings.pad.content;

import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.model.project.AudioPadContent;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import de.tobias.playwall.client.viewcontroller.FileChooserWrapper;
import de.tobias.playwall.common.utils.FileFormats;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

/**
 * Settings related to {@link AudioPadContent}
 */
public class AudioPadContentSettingsContainer extends BasePadContentSettingsContainer<AudioPadContent>
{
	private CheckBox checkboxPlaybackLoop;
	private Label labelFilePath;
	private PlayWallButton buttonShowInFolder;
	private PlayWallButton buttonDelete;
	private Slider volumeSlider;

	public AudioPadContentSettingsContainer(AudioPadContent padContent)
	{
		super(padContent);

		getChildren().addAll(createFileSettings(), new Separator());
		getChildren().addAll(createPlaybackSettings(), new Separator());
		getChildren().addAll(createVolumeSettings(), new Separator());

		isValidProperty.set(true);
	}

	private SettingsRow createFileSettings()
	{
		labelFilePath = new Label(padContent.getMediaPath());
		labelFilePath.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
		labelFilePath.setEllipsisString(" ... ");
		final Tooltip tooltip = new Tooltip();
		tooltip.setText(padContent.getMediaPath());
		tooltip.setShowDelay(Duration.millis(300));
		labelFilePath.setTooltip(tooltip);

		final PlayWallButton buttonChooseFile = new PlayWallButton(FontAwesomeType.FOLDER_OPEN_SOLID);
		buttonChooseFile.setOnAction(this::onButtonFileChooser);

		buttonShowInFolder = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_SHOW_IN_FOLDER), FontAwesomeType.FOLDER_SOLID);
		buttonShowInFolder.setOnAction(this::onButtonShowInFolder);

		buttonDelete = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_DELETE), FontAwesomeType.TRASH_CAN_SOLID);
		buttonDelete.setOnAction(this::onButtonDelete);

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

		volumeSlider.setValue(padContent.getVolume() * 100);
		settingsRowVolume.add(volumeSlider, 1, 0);

		return settingsRowVolume;
	}

	@Override
	public void applySettings()
	{
		padContent.setLoop(checkboxPlaybackLoop.isSelected());
		padContent.setMediaPath(labelFilePath.getText().equals(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_PLACEHOLDER)) ? null : labelFilePath.getText());
		padContent.setVolume(volumeSlider.getValue() / 100.0);
	}

	private void onButtonFileChooser(ActionEvent event)
	{
		final Window owner = ((Node) event.getTarget()).getScene().getWindow();
		final FileChooserWrapper fileChooser = AppContextHolder.getInstance().get(FileChooserWrapper.class);
		fileChooser.setExtensionFilter(FileFormats.FILE_FORMATS.stream().map(format ->
				new FileChooser.ExtensionFilter(
						Localization.getString("FileFormat." + format.contentType().name()),
						format.extensions().stream().map(ext -> "*." + ext).toList()
				)).toList());
		final Optional<Path> path = fileChooser.showOpenFile(owner);

		if(path.isPresent())
		{
			updateLabelFilePath(path.get().toString());
			buttonShowInFolder.setDisable(false);
			buttonDelete.setDisable(false);
		}
	}

	private void onButtonDelete(ActionEvent event)
	{
		updateLabelFilePath(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_PLACEHOLDER));
		buttonShowInFolder.setDisable(true);
		buttonDelete.setDisable(true);
	}

	private void onButtonShowInFolder(ActionEvent event)
	{
		NativeApplication.sharedInstance().showFileInFileViewer(Paths.get(padContent.getMediaPath()));
	}

	private void updateLabelFilePath(String text)
	{
		labelFilePath.setText(text);
		labelFilePath.getTooltip().setText(text);
	}

	private String createSliderStyle(double min, double max, double value)
	{
		final double percentage = 100.0 * (value - min) / (max - min);

		return String.format(Locale.ENGLISH,
				"-slider-track-color: linear-gradient(to right, " +
						"%1$s 0%%, " +
						"%1$s %3$.1f%%, " +
						"%2$s %3$.1f%%, " +
						"%2$s %4$.1f%%, " +
						"%1$s %4$.1f%%, " +
						"%1$s 100%%);",
				ViewConstants.SLIDER_DEFAULT_BACKGROUND_COLOR, ViewConstants.PRIMARY_COLOR, min, percentage);
	}
}
