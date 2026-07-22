package de.tobias.playwall.client.domain.settings.view.settings;

import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.EnumCell;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.tobias.playwall.client.PlayWallMain.DEBUG_FLAG_FILE_NAME;

/**
 * Viewcontroller for the general page in the program settings dialog.
 */
@Slf4j
@ViewController(path = "de/tobias/playwall/client/view/settings/program", view = "ProgramSettingsGeneralPageView", applyToStage = false)
public class ProgramSettingsGeneralViewController extends BaseProgramSettingsViewController
{
	@FXML
	private CheckBox checkboxStartAutoLoadLatestProject;

	@FXML
	private ComboBox<UnsavedChangesMode> comboBoxUnsavedChanges;

	@FXML
	private CheckBox checkboxAutosave;

	@FXML
	private CheckBox checkboxDebugLogging;

	private final ErrorAlertBuilder errorAlertBuilder;

	@InjectConstructor
	public ProgramSettingsGeneralViewController(FluentClient client, ErrorAlertBuilder errorAlertBuilder)
	{
		super(client);
		this.errorAlertBuilder = errorAlertBuilder;
	}

	@Override
	protected void init()
	{
		comboBoxUnsavedChanges.getItems().addAll(UnsavedChangesMode.values());
		comboBoxUnsavedChanges.setButtonCell(new EnumCell<>(Strings.UI_SETTINGS_UNSAVED_CHANGES_MODE_BASE));
		comboBoxUnsavedChanges.setCellFactory(_ -> new EnumCell<>(Strings.UI_SETTINGS_UNSAVED_CHANGES_MODE_BASE));
	}

	@Override
	public void initParameter(Param param)
	{
		checkboxStartAutoLoadLatestProject.setSelected(param.getSettings().isAutoLoadLatestProjectOnStart());
		comboBoxUnsavedChanges.getSelectionModel().select(param.getSettings().getUnsavedChangesMode());

		checkboxAutosave.disableProperty().bind(comboBoxUnsavedChanges.getSelectionModel().selectedItemProperty().isNotEqualTo(UnsavedChangesMode.SAVE));
		checkboxAutosave.setSelected(param.getSettings().isAutosave());

		final Path debugFlag = app.getPath(PathType.CONFIGURATION, DEBUG_FLAG_FILE_NAME);
		checkboxDebugLogging.setSelected(Files.exists(debugFlag));

		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
		param.getSettings().setAutoLoadLatestProjectOnStart(checkboxStartAutoLoadLatestProject.isSelected());
		param.getSettings().setUnsavedChangesMode(comboBoxUnsavedChanges.getSelectionModel().getSelectedItem());
		param.getSettings().setAutosave(checkboxAutosave.isSelected());

		final Path debugFlag = app.getPath(PathType.CONFIGURATION, DEBUG_FLAG_FILE_NAME);
		try
		{
			if(checkboxDebugLogging.isSelected())
			{
				if(!Files.exists(debugFlag))
				{
					Files.createFile(debugFlag);
				}
			}
			else
			{
				Files.deleteIfExists(debugFlag);
			}
		}
		catch(IOException e)
		{
			log.error("Cannot create/delete debug flag file", e);
			errorAlertBuilder.createErrorAlert(null,
					Localization.getString(Strings.UI_SETTINGS_PROGRAM_DEBUG_TITLE),
					Localization.getString(Strings.UI_SETTINGS_PROGRAM_DEBUG_ERROR),
					e,
					getContainingWindow()).showAndWait();
		}
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}
}
