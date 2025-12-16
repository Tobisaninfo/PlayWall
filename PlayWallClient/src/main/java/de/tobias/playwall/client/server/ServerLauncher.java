package de.tobias.playwall.client.server;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = {@InjectConstructor})
public class ServerLauncher
{
	private final ServerPathLookup serverPathLookup;

	private Process serverProcess;

	@SuppressWarnings("java:S899")
	public void launchServer(ServerLauncherProperties properties)
	{
		try
		{
			Logger.info("Server starting, checking files");
			Path resourceFolder = serverPathLookup.getServerInstallationFolder();
			Logger.info("Server folder: " + resourceFolder.toAbsolutePath());

			if(Files.notExists(resourceFolder))
			{
				throw new ServerLaunchException.NotFoundException(resourceFolder);
			}

			final Path jdkFolder = resourceFolder.resolve("PlayWallServer-jdk");
			if(Files.notExists(jdkFolder))
			{
				throw new ServerLaunchException.NotFoundException(jdkFolder);
			}

			final Path javaExecutable = jdkFolder.resolve("bin").resolve(OS.isWindows() ? "java.exe" : "java");

			final String version = ApplicationUtils.getApplication().getInfo().getVersion();
			final Path serverJar = resourceFolder.resolve("PlayWallServer-" + version + ".jar");
			if(Files.notExists(serverJar))
			{
				throw new ServerLaunchException.NotFoundException(serverJar);
			}

			javaExecutable.toFile().setExecutable(true);
			Logger.debug("Set execute permission for: " + javaExecutable.toAbsolutePath());

			final Path loggingPath = ApplicationUtils.getApplication().getPath(PathType.LOG, "server.log");

			final List<String> jvmOptions = List.of("--enable-native-access=ALL-UNNAMED");
			final List<String> programArguments = new ArrayList<>();
			programArguments.add("--logging.file.name=" + loggingPath.toString());
			if(properties.getStoragePath() != null)
			{
				programArguments.add("--de.tobias.playwall.path-provider.base-directory-template=" + properties.getStoragePath());
			}

			final List<String> processCommand = new ArrayList<>();
			processCommand.add(javaExecutable.toAbsolutePath().toString());
			processCommand.addAll(jvmOptions);
			processCommand.add("-jar");
			processCommand.add(serverJar.toString());
			processCommand.addAll(programArguments);
			Logger.info("Server command: " + String.join(" ", processCommand));

			final ProcessBuilder processBuilder = new ProcessBuilder(processCommand);
			processBuilder.directory(resourceFolder.toFile());
			processBuilder.redirectErrorStream(true);
			serverProcess = processBuilder.start();

			Logger.info("Server starting");

			awaitServerStartUp();

			final Thread thread = new Thread(this::printServerLog);
			thread.setDaemon(true);
			thread.start();
		}
		catch(URISyntaxException | IOException e)
		{
			throw new ServerLaunchException.GenericStartupException("Cannot start server", e);
		}
	}

	private void awaitServerStartUp() throws IOException
	{
		boolean started = false;
		final Pattern readyPattern = Pattern.compile("Started PlayWallServerMain .*");
		final Pattern portUsedPattern = Pattern.compile("Web server failed to start\\. Port \\d+ was already in use\\.");

		final BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
		final List<String> lines = new ArrayList<>();

		String line;
		while((line = reader.readLine()) != null)
		{
			lines.add(line);
			if(readyPattern.matcher(line).find())
			{
				started = true;
				Logger.info("Server successfully started");
				break;
			}
			if(portUsedPattern.matcher(line).find())
			{
				throw new ServerLaunchException.PortInUseException();
			}
		}

		if(!started)
		{
			Logger.error("Server failed to start: \n" + String.join("\n", lines));
			throw new ServerLaunchException.GenericStartupException();
		}
	}

	@SneakyThrows
	private void printServerLog()
	{
		try(BufferedWriter writer = Files.newBufferedWriter(ApplicationUtils.getApplication().getPath(PathType.LOG, "client_server.log")))
		{
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream())))
			{
				String line;
				while((line = reader.readLine()) != null)
				{
					writer.write(line);
					writer.newLine();
					writer.flush();
				}
			}
			catch(IOException e)
			{
				Logger.error(e);
			}
			serverProcess.waitFor();
			writer.write("Server exit code " + serverProcess.exitValue());
			writer.newLine();
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
