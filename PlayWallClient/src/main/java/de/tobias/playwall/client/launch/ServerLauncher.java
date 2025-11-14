package de.tobias.playwall.client.launch;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.client.PlayWallMain;
import de.tobias.playwall.client.appcontext.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServerLauncher
{
	private Process serverProcess;

	@SuppressWarnings("java:S899")
	public void launchServer()
	{
		try
		{
			Logger.info("Server starting, checking files");
			final Path resourceFolder = Paths.get(PlayWallMain.class.getProtectionDomain()
							.getCodeSource()
							.getLocation()
							.toURI()).getParent()
					.resolve("server");
			Logger.info("Server folder: " + resourceFolder.toAbsolutePath());

			if(Files.notExists(resourceFolder))
			{
				throw new ServerLaunchException(resourceFolder + " not found");
			}

			final Path jdkFolder = resourceFolder.resolve("PlayWallServer-jdk");
			if(Files.notExists(jdkFolder))
			{
				throw new ServerLaunchException(jdkFolder + " not found");
			}

			final Path javaExecutable = jdkFolder.resolve("bin").resolve(OS.isWindows() ? "java.exe" : "java");

			final String version = ApplicationUtils.getApplication().getInfo().getVersion();
			final Path serverJar = resourceFolder.resolve("PlayWallServer-" + version + ".jar");
			if(Files.notExists(serverJar))
			{
				throw new ServerLaunchException(serverJar + " not found");
			}

			javaExecutable.toFile().setExecutable(true);
			Logger.debug("Set execute permission for: " + javaExecutable.toAbsolutePath());

			final Path loggingPath = ApplicationUtils.getApplication().getPath(PathType.LOG, "server.log");

			final List<String> jvmOptions = List.of("--enable-native-access=ALL-UNNAMED");
			final List<String> programArguments = List.of("--logging.file.name=" + loggingPath.toString());

			final List<String> processCommand = new ArrayList<>();
			processCommand.add(javaExecutable.toAbsolutePath().toString());
			processCommand.addAll(jvmOptions);
			processCommand.add("-jar");
			processCommand.add(serverJar.toString());
			processCommand.addAll(programArguments);
			Logger.info("Server command: " + String.join(" ", processCommand));

			final ProcessBuilder processBuilder = new ProcessBuilder(processCommand);
			processBuilder.directory(resourceFolder.toFile());
			serverProcess = processBuilder.start();

			Logger.info("Server started");
		}
		catch(URISyntaxException | IOException e)
		{
			throw new ServerLaunchException("Cannot start server", e);
		}
	}

	public void stopServer()
	{
		if(serverProcess != null && serverProcess.isAlive())
		{
			Logger.info("Server stopping...");
			serverProcess.destroy();
			try
			{
				if(!serverProcess.waitFor(3, java.util.concurrent.TimeUnit.SECONDS))
				{
					Logger.info("Server not responding, force kill...");
					serverProcess.destroyForcibly();
				}
			}
			catch(InterruptedException _)
			{
				Thread.currentThread().interrupt();
				Logger.info("Server shutdown interrupted, forcing termination...");
				serverProcess.destroyForcibly();
			}
			Logger.info("Server stopped");
		}
	}
}
