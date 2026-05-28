package de.tobias.playwall.client.view.launch.tasks;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import javafx.scene.control.Label;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = {@InjectConstructor})
public class FetchProgramSettingsLaunchTask extends LaunchTask
{
	private final FluentClient client;
	private final ClientSettingsController settingsController;

	@Override
	public LaunchResult launch(Label progressLabel)
	{
		try
		{
			settingsController.setSettings(client.settings().get());
			settingsController.setOutputDeviceNames(client.getOutputDevices());
			return new SuccessResult();
		}
		catch(PlayWallApiException e)
		{
			return new FailureResult(Localization.getString("ui.application_loading.error.settings", e.getMessage()), e, false);
		}
	}
}
