package de.tobias.playwall.client.server;

import de.tobias.playwall.client.CommandLineOptions;
import de.tobias.playwall.client.PlayWallMain;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = {@InjectConstructor})
class ServerPathLookup
{
	private final CommandLineOptions commandLineOptions;

	Path getServerInstallationFolder() throws URISyntaxException
	{
		if(commandLineOptions.hasOption(CommandLineOptions.SERVER_PATH))
		{
			return Paths.get(commandLineOptions.getOptionValue(CommandLineOptions.SERVER_PATH));
		}

		return Paths.get(PlayWallMain.class.getProtectionDomain()
						.getCodeSource()
						.getLocation()
						.toURI()).getParent()
				.resolve("server");
	}
}
