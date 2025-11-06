package de.tobias.playwall.client;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.client.di.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ServerLauncher
{
	private Process serverProcess;

	public void launchServer()
	{
		try
		{
			final Path resourceFolder = Paths.get(PlayWallMain.class.getProtectionDomain()
					.getCodeSource()
					.getLocation()
					.toURI()).getParent()
					.resolve("server");

			if (Files.notExists(resourceFolder)) {
				throw new RuntimeException("PlayWallServer not found");
			}

			final Path jdkFolder = resourceFolder.resolve("PlayWallServer-jdk");
			if(Files.notExists(jdkFolder))
			{
				throw new RuntimeException("PlayWallServer-jdk not found");
			}

			Path javaExecutable = jdkFolder.resolve("bin").resolve(OS.isWindows() ? "java.exe" : "java");

			final Path serverJar = resourceFolder.resolve("PlayWallServer-8.0.0.jar");
			if(Files.notExists(serverJar))
			{
				throw new RuntimeException("PlayWallServer-8.0.0.jar not found");
			}

			final File javaFile = javaExecutable.toFile();
			javaFile.setExecutable(true);

			ProcessBuilder processBuilder = new ProcessBuilder(javaFile.getAbsolutePath(), "-jar", serverJar.toString());
			processBuilder.directory(resourceFolder.toFile());
			serverProcess = processBuilder.start();
		}
		catch(URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void stopServer()
	{
		if(serverProcess != null && serverProcess.isAlive())
		{
			serverProcess.destroy(); // Sanft stoppen
			try
			{
				if(!serverProcess.waitFor(3, java.util.concurrent.TimeUnit.SECONDS))
				{
					Logger.info("Server not responding, force kill...");
					serverProcess.destroyForcibly(); // Hart beenden
				}
			}
			catch(InterruptedException ignored)
			{
			}
		}
	}
}
