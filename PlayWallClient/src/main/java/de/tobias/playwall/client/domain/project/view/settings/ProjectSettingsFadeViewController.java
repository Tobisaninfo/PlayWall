package de.tobias.playwall.client.domain.project.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.project.FadeSettings;
import de.tobias.playwall.client.net.FluentClient;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import lombok.SneakyThrows;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Viewcontroller for the general page in the project settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsFadePageView", applyToStage = false)
public class ProjectSettingsFadeViewController extends BaseProjectSettingsViewController
{
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.GERMAN);

	static
	{
		NUMBER_FORMAT.setMinimumFractionDigits(1);
		NUMBER_FORMAT.setMaximumFractionDigits(1);
	}

	@FXML
	private Spinner<Double> fadeInDurationSpinner;
	@FXML
	private CheckBox fadeInPlayCheckBox;
	@FXML
	private CheckBox fadeInResumeCheckBox;
	@FXML
	private Spinner<Double> fadeOutDurationSpinner;
	@FXML
	private CheckBox fadeOutPauseCheckBox;
	@FXML
	private CheckBox fadeOutStopCheckBox;
	@FXML
	private CheckBox fadeOutEndOfFileCheckBox;

	@InjectConstructor
	public ProjectSettingsFadeViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		final FadeSettings fadeSettings = param.getProjectMetadata().getFadeSettings();

		fadeInDurationSpinner.getEditor().setText(NUMBER_FORMAT.format(fadeSettings.getFadeInDuration()));
		fadeInPlayCheckBox.setSelected(fadeSettings.getFadeInOnPlay());
		fadeInResumeCheckBox.setSelected(fadeSettings.getFadeInOnResume());

		fadeOutDurationSpinner.getEditor().setText(NUMBER_FORMAT.format(fadeSettings.getFadeOutDuration()));
		fadeOutPauseCheckBox.setSelected(fadeSettings.getFadeOutOnPause());
		fadeOutStopCheckBox.setSelected(fadeSettings.getFadeOutOnStop());
		fadeOutEndOfFileCheckBox.setSelected(fadeSettings.getFadeOutOnEndOfFile());

		this.isValidProperty.set(true);
	}

	@SneakyThrows
	@Override
	public void applySettings(Param param)
	{
		final FadeSettings fadeSettings = param.getProjectMetadata().getFadeSettings();

		fadeSettings.setFadeInDuration(NUMBER_FORMAT.parse(fadeInDurationSpinner.getEditor().getText()).doubleValue());
		fadeSettings.setFadeInOnPlay(fadeInPlayCheckBox.isSelected());
		fadeSettings.setFadeInOnResume(fadeInResumeCheckBox.isSelected());

		fadeSettings.setFadeOutDuration(NUMBER_FORMAT.parse(fadeOutDurationSpinner.getEditor().getText()).doubleValue());
		fadeSettings.setFadeOutOnPause(fadeOutPauseCheckBox.isSelected());
		fadeSettings.setFadeOutOnStop(fadeOutStopCheckBox.isSelected());
		fadeSettings.setFadeOutOnEndOfFile(fadeOutEndOfFileCheckBox.isSelected());
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}
}
