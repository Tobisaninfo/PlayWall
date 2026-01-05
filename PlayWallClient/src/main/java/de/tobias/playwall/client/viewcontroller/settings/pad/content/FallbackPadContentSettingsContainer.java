package de.tobias.playwall.client.viewcontroller.settings.pad.content;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.model.project.AudioPadContent;
import de.tobias.playwall.client.model.project.FallbackPadContent;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import de.tobias.playwall.client.viewcontroller.FileChooserWrapper;
import de.tobias.playwall.client.viewcontroller.settings.pad.BasePadSettingsViewController;
import de.tobias.playwall.common.utils.FileFormats;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * Fallback settings to allow to choose a media file if the pad is empty.
 */
public class FallbackPadContentSettingsContainer extends BasePadContentSettingsContainer<FallbackPadContent>
{
	private final FluentClient fluentClient;

	public FallbackPadContentSettingsContainer(UUID padId, FluentClient fluentClient)
	{
		super(null, padId);
		this.fluentClient = fluentClient;

		getChildren().addAll(createFileSettings(), new Separator());

		isValidProperty.set(true);
	}

	private SettingsRow createFileSettings()
	{
		final PlayWallButton buttonChooseFile = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_CHOOSE_PATH), FontAwesomeType.FOLDER_OPEN_SOLID);
		buttonChooseFile.setOnAction(this::onButtonFileChooser);

		final SettingsRow settingsRowFile = new SettingsRow();
		settingsRowFile.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_FILE));
		settingsRowFile.setIcon(FontAwesomeType.FILE_SOLID);
		settingsRowFile.add(buttonChooseFile, 1, 0);
		return settingsRowFile;
	}

	@Override
	public void applySettings(BasePadSettingsViewController.Param param)
	{
	}

	@Override
	public void cleanup()
	{
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
	}
}
