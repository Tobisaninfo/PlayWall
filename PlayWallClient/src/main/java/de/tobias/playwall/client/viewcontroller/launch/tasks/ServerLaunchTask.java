package de.tobias.playwall.client.viewcontroller.launch.tasks;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.server.ServerLaunchException;
import de.tobias.playwall.client.server.ServerLauncher;
import javafx.scene.control.Label;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Arrays;

@Service
@AllArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = {@InjectConstructor})
public class ServerLaunchTask extends LaunchTask
{
	private final App app;
	private final ServerLauncher serverLauncher;

	@Override
	public LaunchResult launch(Label progressLabel)
	{
		final String[] args = app.getProgramArgs();
		if(!(args != null && args.length != 0 && Arrays.binarySearch(args, "--standalone") >= 0))
		{
			Runtime.getRuntime().addShutdownHook(new Thread(serverLauncher::stopServer));
			try
			{
				serverLauncher.launchServer();
			}
			catch(ServerLaunchException.NotFoundException e)
			{
				return new FailureResult(Localization.getString("ui.application_loading.error.server.not_found", e.getPath()), e, false);
			}
			catch(ServerLaunchException.PortInUseException e)
			{
				return new FailureResult(Localization.getString("ui.application_loading.error.server.port_in_use", 10023), e, false);  // TODO: Port hard coded
			}
			catch(ServerLaunchException.GenericStartupException e)
			{
				return new FailureResult(Localization.getString(Localization.getString("ui.application_loading.error.server.generic", e.getMessage())), e, true);
			}
		}
		return new SuccessResult();
	}
}
