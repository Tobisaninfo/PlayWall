package de.tobias.playwall.client.domain.project.view.settings;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.companion.CompanionPluginExporter;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Viewcontroller for the companion page in the project settings dialog.
 * Shows the setup instructions for the Bitfocus Companion plugin and exports the bundled plugin file.
 */
@Slf4j
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsCompanionPageView", applyToStage = false)
public class ProjectSettingsCompanionViewController extends BaseProjectSettingsViewController
{
	@FXML
	private Button buttonExport;

	@FXML
	private Label labelExportResult;

	private final FileChooserWrapper fileChooserWrapper;
	private final ErrorAlertBuilder errorAlertBuilder;
	private final CompanionPluginExporter exporter = new CompanionPluginExporter();

	@InjectConstructor
	ProjectSettingsCompanionViewController(FluentClient client, FileChooserWrapper fileChooserWrapper, ErrorAlertBuilder errorAlertBuilder)
	{
		super(client);
		this.fileChooserWrapper = fileChooserWrapper;
		this.errorAlertBuilder = errorAlertBuilder;
	}

	@Override
	protected void init()
	{
		labelExportResult.managedProperty().bind(labelExportResult.visibleProperty());
		labelExportResult.setVisible(false);

		if(!exporter.isPluginAvailable())
		{
			buttonExport.setDisable(true);
			labelExportResult.setText(Localization.getString(Strings.UI_SETTINGS_PROJECT_COMPANION_UNAVAILABLE));
			labelExportResult.setVisible(true);
		}
	}

	@Override
	public void initParameter(Param param)
	{
		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
		// Nothing to do
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}

	@FXML
	protected void onButtonExport(ActionEvent event)
	{
		fileChooserWrapper.showOpenFolder(getContainingWindow()).ifPresent(this::exportPlugin);
	}

	private void exportPlugin(Path folder)
	{
		try
		{
			final Path file = exporter.export(folder);
			labelExportResult.setText(Localization.getString(Strings.UI_SETTINGS_PROJECT_COMPANION_EXPORT_SUCCESS, file.toString()));
			labelExportResult.setVisible(true);
		}
		catch(IOException e)
		{
			log.error("Cannot export companion plugin to {}", folder, e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_SETTINGS_PROJECT_COMPANION_EXPORT_ERROR), e.getMessage(), e, getContainingWindow()).showAndWait();
		}
	}
}
