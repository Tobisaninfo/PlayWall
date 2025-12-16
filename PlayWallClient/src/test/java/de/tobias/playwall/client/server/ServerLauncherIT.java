package de.tobias.playwall.client.server;

import de.tobias.playwall.client.CommandLineOptions;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.extensions.AppEnvironmentSetup;
import de.tobias.playwall.client.extensions.LoggerSetup;
import de.tobias.playwall.client.net.Client;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Paths;

import static org.mockito.Mockito.when;

@ExtendWith(LoggerSetup.class)
@ExtendWith(AppEnvironmentSetup.class)
class ServerLauncherIT
{
	private final CommandLineOptions options = Mockito.mock(CommandLineOptions.class);

	private ServerLauncherProperties serverLauncherProperties;
	private ServerLauncher serverLauncher;
	private Client client;

	@BeforeEach
	void init()
	{
		final AppContext context = AppContextHolder.getInstance();
		context.registerLazySingleton(CommandLineOptions.class, _ -> options);

		when(options.hasOption(CommandLineOptions.SERVER_PATH)).thenReturn(true);
		when(options.getOptionValue(CommandLineOptions.SERVER_PATH)).thenReturn(Paths.get("target/build/server").toAbsolutePath().toString());

		serverLauncherProperties = ServerLauncherProperties.builder().storagePath(Paths.get("./target").toAbsolutePath().toString()).build();

		serverLauncher = context.get(ServerLauncher.class);
		client = context.get(Client.class);
	}

	@AfterEach
	void tearDown()
	{
		serverLauncher.stopServer();
	}

	@Test
	@SuppressWarnings("java:S2699")
	void testLaunchServerSuccessful()
	{
		serverLauncher.launchServer(serverLauncherProperties);

		client.connect();
	}

	@Test
	void testLaunchServerPortBusy() throws IOException
	{
		try(var _ = new ServerSocket(10023))
		{
			Assertions.assertThatThrownBy(() -> serverLauncher.launchServer(serverLauncherProperties))
					.isInstanceOf(ServerLaunchException.PortInUseException.class);
		}
	}

	@Test
	void testLaunchServerFolderNotFound()
	{
		when(options.getOptionValue(CommandLineOptions.SERVER_PATH)).thenReturn("target/build");
		Assertions.assertThatThrownBy(() -> serverLauncher.launchServer(serverLauncherProperties))
				.isInstanceOf(ServerLaunchException.NotFoundException.class);
	}
}
