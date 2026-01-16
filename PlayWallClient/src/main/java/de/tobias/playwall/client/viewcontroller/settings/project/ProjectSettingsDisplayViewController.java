package de.tobias.playwall.client.viewcontroller.settings.project;

import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.EnumCell;
import de.tobias.playwall.common.api.project.model.ProjectTimeMode;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

/**
 * Viewcontroller for the view page in the project settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsDisplayPageView", applyToStage = false)
public class ProjectSettingsDisplayViewController extends BaseProjectSettingsViewController
{
	@FXML
	private ComboBox<ProjectTimeMode> comboBoxTime;

	@InjectConstructor
	public ProjectSettingsDisplayViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		comboBoxTime.getItems().addAll(ProjectTimeMode.values());
		comboBoxTime.setButtonCell(new EnumCell<>(Strings.UI_SETTINGS_PROJECT_TIME_MODE_BASE));
		comboBoxTime.setCellFactory(list -> new EnumCell<>(Strings.UI_SETTINGS_PROJECT_TIME_MODE_BASE));
		comboBoxTime.getSelectionModel().select(0);

		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
	}

	@Override
	public void cleanup()
	{
	}
}
