package de.tobias.playwall.client.viewcontroller.launch.tasks;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.net.Client;
import javafx.application.Platform;
import javafx.scene.control.Label;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = {@InjectConstructor})
public class ClientConnectLaunchTask extends LaunchTask
{
	private final Client client;

	@Override
	public LaunchResult launch(Label progressLabel)
	{
		try
		{
			client.connectWithRetries(60, ((currentTry, maximumNumberOfTries) ->
					Platform.runLater(() -> progressLabel.setText(Localization.getString("ui.application_loading.label", currentTry, maximumNumberOfTries)))));
			return new SuccessResult();
		}
		catch(Exception e)
		{
			return new FailureResult(Localization.getString("ui.application_loading.error.connect", e.getMessage()), e, false);
		}
	}
}
