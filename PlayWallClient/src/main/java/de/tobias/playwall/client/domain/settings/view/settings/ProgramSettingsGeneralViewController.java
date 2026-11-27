package de.tobias.playwall.client.domain.settings.view.settings;

import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.settings.LinuxUiScaleService;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.EnumCell;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.tobias.playwall.client.PlayWallMain.DEBUG_FLAG_FILE_NAME;

/**
 * Viewcontroller for the general page in the program settings dialog.
 */
@Slf4j
@SuppressWarnings("java:S110")
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
	private CheckBox checkboxIsAutomaticUpdateSearch;

	@FXML
	private CheckBox checkboxDebugLogging;

	@FXML
	private ComboBox<Integer> comboBoxUiScale;

	@FXML
	private SettingsRow settingsRowUiScale;

	@FXML
	private Separator separatorUiScale;

	private final ErrorAlertBuilder errorAlertBuilder;
	private final LinuxUiScaleService uiScaleService;

	@InjectConstructor
	public ProgramSettingsGeneralViewController(FluentClient client, ErrorAlertBuilder errorAlertBuilder, LinuxUiScaleService uiScaleService)
	{
		super(client);
		this.errorAlertBuilder = errorAlertBuilder;
		this.uiScaleService = uiScaleService;
	}

	@Override
	protected void init()
	{
		comboBoxUnsavedChanges.getItems().addAll(UnsavedChangesMode.values());
		comboBoxUnsavedChanges.setButtonCell(new EnumCell<>(UnsavedChangesMode.class));
		comboBoxUnsavedChanges.setCellFactory(_ -> new EnumCell<>(UnsavedChangesMode.class));

		comboBoxUiScale.getItems().addAll(LinuxUiScaleService.UI_SCALE_AUTO, 100, 125, 150, 175, 200);
		comboBoxUiScale.setConverter(new StringConverter<>()
		{
			@Override
			public String toString(Integer value)
			{
				return value == null || value == LinuxUiScaleService.UI_SCALE_AUTO
						? Localization.getString(Strings.UI_SETTINGS_PROGRAM_DISPLAY_SCALE_AUTO)
						: value + "%";
			}

			@Override
			public Integer fromString(String string)
			{
				return LinuxUiScaleService.UI_SCALE_AUTO;
			}
		});

		// glass.gtk.uiScale only affects the GTK/Linux Glass backend; macOS/Windows read the
		// display scale natively and ignore it, so the control would be a no-op there.
		final boolean isLinux = OS.isLinux();
		settingsRowUiScale.setVisible(isLinux);
		settingsRowUiScale.setManaged(isLinux);
		separatorUiScale.setVisible(isLinux);
		separatorUiScale.setManaged(isLinux);
	}

	@Override
	public void initParameter(Param param)
	{
		checkboxStartAutoLoadLatestProject.setSelected(param.getSettings().isAutoLoadLatestProjectOnStart());
		comboBoxUnsavedChanges.getSelectionModel().select(param.getSettings().getUnsavedChangesMode());

		checkboxAutosave.disableProperty().bind(comboBoxUnsavedChanges.getSelectionModel().selectedItemProperty().isNotEqualTo(UnsavedChangesMode.SAVE));
		checkboxAutosave.setSelected(param.getSettings().isAutosave());

		checkboxIsAutomaticUpdateSearch.setSelected(param.getSettings().isAutomaticUpdateSearch());

		final Path debugFlag = app.getPath(PathType.CONFIGURATION, DEBUG_FLAG_FILE_NAME);
		checkboxDebugLogging.setSelected(Files.exists(debugFlag));

		final Integer uiScale = uiScaleService.readScale();
		comboBoxUiScale.getSelectionModel().select(uiScale);

		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
		param.getSettings().setAutoLoadLatestProjectOnStart(checkboxStartAutoLoadLatestProject.isSelected());
		param.getSettings().setUnsavedChangesMode(comboBoxUnsavedChanges.getSelectionModel().getSelectedItem());
		param.getSettings().setAutosave(checkboxAutosave.isSelected());
		param.getSettings().setAutomaticUpdateSearch(checkboxIsAutomaticUpdateSearch.isSelected());

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

		try
		{
			uiScaleService.applyScale(comboBoxUiScale.getSelectionModel().getSelectedItem());
		}
		catch(IOException e)
		{
			log.error("Cannot write UI scale file", e);
			errorAlertBuilder.createErrorAlert(null,
					Localization.getString(Strings.UI_SETTINGS_PROGRAM_DISPLAY_TITLE),
					Localization.getString(Strings.UI_SETTINGS_PROGRAM_DISPLAY_ERROR),
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
