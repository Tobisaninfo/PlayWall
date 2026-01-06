package de.tobias.playwall.client.viewcontroller.settings.pad.content;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.model.project.FallbackPadContent;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import de.tobias.playwall.client.viewcontroller.settings.pad.BasePadSettingsViewController;
import de.tobias.playwall.client.viewcontroller.settings.pad.PadSettingsViewController;
import javafx.scene.control.Separator;

import java.util.UUID;

/**
 * Fallback settings to allow to choose a media file if the pad is empty.
 */
public class FallbackPadContentSettingsContainer extends BasePadContentSettingsContainer<FallbackPadContent>
{
	public FallbackPadContentSettingsContainer(UUID padId, PadSettingsViewController parentDialog)
	{
		super(null, padId, parentDialog);

		getChildren().addAll(createFileSettings(), new Separator());

		isValidProperty.set(true);
	}

	private SettingsRow createFileSettings()
	{
		final PlayWallButton buttonChooseFile = new PlayWallButton(Localization.getString(Strings.UI_SETTINGS_PAD_FILE_CHOOSE_PATH), FontAwesomeType.FOLDER_OPEN_SOLID);
		buttonChooseFile.setOnAction(parentDialog::onButtonFileChooser);

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
}
