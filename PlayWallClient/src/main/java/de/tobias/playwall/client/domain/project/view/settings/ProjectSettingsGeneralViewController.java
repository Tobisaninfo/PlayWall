package de.tobias.playwall.client.domain.project.view.settings;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.PlayWallToggleButton;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
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

	@FXML
	private Slider eofWarningTimeSlider;

	@FXML
	private Label eofWarningTimeLabel;

	@FXML
	private PlayWallToggleButton toggleButtonPlaybackModeSolo;

	@FXML
	private PlayWallToggleButton toggleButtonPlaybackModeMulti;

	@InjectConstructor
	public ProjectSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	protected void init()
	{
		super.init();
		eofWarningTimeLabel.textProperty().bind(
				Bindings.createStringBinding(
						() -> Localization.getString(
								Strings.UI_SETTINGS_PROJECT_WARNING_EOF_SEC,
								String.format("%.1f", eofWarningTimeSlider.getValue())
						),
						eofWarningTimeSlider.valueProperty()
				)
		);
	}

	@Override
	public void initParameter(Param param)
	{
		textFieldName.setText(param.getProjectMetadata().getName());
		spinnerNumberOfHorizontalPads.getValueFactory().setValue(param.getProjectMetadata().getNumberOfHorizontalPads());
		spinnerNumberOfVerticalPads.getValueFactory().setValue(param.getProjectMetadata().getNumberOfVerticalPads());
		eofWarningTimeSlider.setValue(param.getProjectMetadata().getEofWarningTime());
		toggleButtonPlaybackModeSolo.setSelected(param.getProjectMetadata().getIsSoloMode());
		toggleButtonPlaybackModeMulti.setSelected(!param.getProjectMetadata().getIsSoloMode());

		this.isValidProperty.bind(textFieldName.textProperty().isNotEmpty()
				.and(spinnerNumberOfHorizontalPads.valueProperty().isNotNull()
						.and(spinnerNumberOfVerticalPads.valueProperty().isNotNull())));
	}

	@Override
	public void applySettings(Param param)
	{
		final ProjectMetadata projectMetadata = param.getProjectMetadata();

		projectMetadata.setName(textFieldName.getText());
		projectMetadata.setNumberOfHorizontalPads(spinnerNumberOfHorizontalPads.getValue());
		projectMetadata.setNumberOfVerticalPads(spinnerNumberOfVerticalPads.getValue());
		projectMetadata.setEofWarningTime(eofWarningTimeSlider.getValue());
		projectMetadata.setIsSoloMode(toggleButtonPlaybackModeSolo.isSelected());
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}
}
