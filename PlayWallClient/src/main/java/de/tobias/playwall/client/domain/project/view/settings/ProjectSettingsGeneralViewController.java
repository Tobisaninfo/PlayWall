package de.tobias.playwall.client.domain.project.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
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

	@FXML
	private Spinner<Integer> spinnerNumberOfHorizontalPads;

	@FXML
	private Spinner<Integer> spinnerNumberOfVerticalPads;

	@InjectConstructor
	public ProjectSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		textFieldName.setText(param.getProjectMetadata().getName());
		spinnerNumberOfHorizontalPads.getValueFactory().setValue(param.getProjectMetadata().getNumberOfHorizontalPads());
		spinnerNumberOfVerticalPads.getValueFactory().setValue(param.getProjectMetadata().getNumberOfVerticalPads());

		this.isValidProperty.bind(textFieldName.textProperty().isNotEmpty()
				.and(spinnerNumberOfHorizontalPads.valueProperty().isNotNull()
				.and(spinnerNumberOfVerticalPads.valueProperty().isNotNull())));
	}

	@Override
	public void applySettings(Param param)
	{
		param.getProjectMetadata().setName(textFieldName.getText());
		param.getProjectMetadata().setNumberOfHorizontalPads(spinnerNumberOfHorizontalPads.getValue());
		param.getProjectMetadata().setNumberOfVerticalPads(spinnerNumberOfVerticalPads.getValue());
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}
}
