package de.tobias.playwall.client.domain.project.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Viewcontroller for the general page in the project settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsGeneralPageView", applyToStage = false)
public class ProjectSettingsGeneralViewController extends BaseProjectSettingsViewController
{
	@FXML
	private TextField textFieldName;

	@InjectConstructor
	public ProjectSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		textFieldName.setText(param.getProjectMetadata().getName());
		this.isValidProperty.bind(textFieldName.textProperty().isNotEmpty());
	}

	@Override
	public void applySettings(Param param)
	{
		param.getProjectMetadata().setName(textFieldName.getText());
	}

	@Override
	public void cleanup()
	{
	}
}
