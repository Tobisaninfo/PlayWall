package de.tobias.playwall.client.domain.project.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.common.FadeSettingsController;
import de.tobias.playwall.client.domain.project.FadeSettings;
import de.tobias.playwall.client.net.FluentClient;
import javafx.fxml.FXML;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * Viewcontroller for the general page in the project settings dialog.
 */
@SuppressWarnings("java:S110")
@ViewController(path = "de/tobias/playwall/client/view/settings/project", view = "ProjectSettingsFadePageView", applyToStage = false)
public class ProjectSettingsFadeViewController extends BaseProjectSettingsViewController
{
	@FXML
	@Getter
	private FadeSettingsController fadeSettingsController;

	@InjectConstructor
	public ProjectSettingsFadeViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		fadeSettingsController.initParameter(param.getProjectMetadata().getFadeSettings());
		this.isValidProperty.set(true);
	}

	@SneakyThrows
	@Override
	public void applySettings(Param param)
	{
		final FadeSettings fadeSettings = param.getProjectMetadata().getFadeSettings();
		fadeSettingsController.applySettings(fadeSettings);
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}
}
