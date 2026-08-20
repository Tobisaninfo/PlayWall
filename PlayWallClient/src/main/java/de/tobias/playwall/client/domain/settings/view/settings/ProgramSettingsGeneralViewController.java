package de.tobias.playwall.client.domain.settings.view.settings;

import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.thecodelabs.utils.util.OS;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.tobias.playwall.client.PlayWallMain.DEBUG_FLAG_FILE_NAME;
import static de.tobias.playwall.client.PlayWallMain.UI_SCALE_FILE_NAME;

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

	@FXML
	private ComboBox<Integer> comboBoxUiScale;

	@FXML
	private SettingsRow settingsRowUiScale;

	@FXML
	private Separator separatorUiScale;

	private static final int UI_SCALE_AUTO = 0;

	/**
	 * Glass only picks up {@code glass.gtk.uiScale} when it's present as a real JVM argument at
	 * process launch, not via {@link System#setProperty} from within an already-running JVM. For
	 * a jpackage app-image, the launch arguments come from the native launcher's {@code .cfg}
	 * file (e.g. {@code lib/app/PlayWall.cfg}), so that's the file that actually needs updating.
	 */
	private static final String CFG_UI_SCALE_PREFIX = "java-options=-Dglass.gtk.uiScale=";

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

		comboBoxUiScale.getItems().addAll(UI_SCALE_AUTO, 100, 125, 150, 175, 200);
		comboBoxUiScale.setConverter(new StringConverter<>()
		{
			@Override
			public String toString(Integer value)
			{
				return value == null || value == UI_SCALE_AUTO
						? Localization.getString(Strings.UI_SETTINGS_PROGRAM_DISPLAY_SCALE_AUTO)
						: value + "%";
			}

			@Override
			public Integer fromString(String string)
			{
				return UI_SCALE_AUTO;
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

		final Path debugFlag = app.getPath(PathType.CONFIGURATION, DEBUG_FLAG_FILE_NAME);
		checkboxDebugLogging.setSelected(Files.exists(debugFlag));

		final Integer uiScale = readUiScale();
		comboBoxUiScale.getSelectionModel().select(uiScale);

		this.isValidProperty.set(true);
	}

	private Integer readUiScale()
	{
		final Path uiScaleFile = app.getPath(PathType.CONFIGURATION, UI_SCALE_FILE_NAME);
		if(Files.exists(uiScaleFile))
		{
			try
			{
				final String content = Files.readString(uiScaleFile).trim();
				if(!content.isEmpty())
				{
					return Integer.parseInt(content);
				}
			}
			catch(IOException | NumberFormatException e)
			{
				log.warn("Cannot read UI scale file", e);
			}
		}
		return UI_SCALE_AUTO;
	}

	/**
	 * Locates the jpackage native launcher's {@code .cfg} file next to the currently running
	 * launcher binary (e.g. {@code <AppRoot>/bin/PlayWall} -> {@code <AppRoot>/lib/app/PlayWall.cfg}).
	 * Returns empty when not running from a jpackage app-image (e.g. a dev run), in which case
	 * there's nothing to update.
	 */
	private static Optional<Path> findLauncherConfigFile()
	{
		final Optional<String> command = ProcessHandle.current().info().command();
		if(command.isEmpty())
		{
			return Optional.empty();
		}

		final Path launcher = Path.of(command.get());
		final Path binDir = launcher.getParent();
		final Path appRoot = binDir == null ? null : binDir.getParent();
		if(appRoot == null)
		{
			return Optional.empty();
		}

		final Path cfgFile = appRoot.resolve("lib").resolve("app").resolve(launcher.getFileName() + ".cfg");
		return Files.isRegularFile(cfgFile) ? Optional.of(cfgFile) : Optional.empty();
	}

	private void updateLauncherConfig(Integer selectedScale) throws IOException
	{
		final Optional<Path> cfgFile = findLauncherConfigFile();
		if(cfgFile.isEmpty())
		{
			return;
		}

		final List<String> lines = new ArrayList<>(Files.readAllLines(cfgFile.get()));
		int lastJavaOptionsIndex = -1;
		for(int i = 0; i < lines.size(); i++)
		{
			final String line = lines.get(i);
			if(line.startsWith(CFG_UI_SCALE_PREFIX))
			{
				lines.remove(i);
				i--;
				continue;
			}
			if(line.startsWith("java-options="))
			{
				lastJavaOptionsIndex = i;
			}
		}

		if(selectedScale != null && selectedScale != UI_SCALE_AUTO)
		{
			final String newLine = CFG_UI_SCALE_PREFIX + selectedScale + "%";
			if(lastJavaOptionsIndex >= 0)
			{
				lines.add(lastJavaOptionsIndex + 1, newLine);
			}
			else
			{
				lines.add(newLine);
			}
		}

		Files.write(cfgFile.get(), lines);
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

		final Path uiScaleFile = app.getPath(PathType.CONFIGURATION, UI_SCALE_FILE_NAME);
		try
		{
			final Integer selectedScale = comboBoxUiScale.getSelectionModel().getSelectedItem();
			if(selectedScale == null || selectedScale == UI_SCALE_AUTO)
			{
				Files.deleteIfExists(uiScaleFile);
			}
			else
			{
				Files.writeString(uiScaleFile, String.valueOf(selectedScale));
			}

			updateLauncherConfig(selectedScale);
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
