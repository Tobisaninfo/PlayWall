package de.tobias.playwall.client.domain.project.view.settings;

import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.EnumCell;
import de.tobias.playwall.common.api.common.TimeMode;
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
	private ComboBox<TimeMode> comboBoxTime;

	@InjectConstructor
	public ProjectSettingsDisplayViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	protected void init()
	{
		comboBoxTime.getItems().addAll(TimeMode.values());
		comboBoxTime.setButtonCell(new EnumCell<>(Strings.UI_SETTINGS_PROJECT_TIME_MODE_BASE));
		comboBoxTime.setCellFactory(_ -> new EnumCell<>(Strings.UI_SETTINGS_PROJECT_TIME_MODE_BASE));
	}

	@Override
	public void initParameter(Param param)
	{
		comboBoxTime.getSelectionModel().select(param.projectMetadata.getTimeMode());

		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
		param.getProjectMetadata().setTimeMode(comboBoxTime.getSelectionModel().getSelectedItem());
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}
}
