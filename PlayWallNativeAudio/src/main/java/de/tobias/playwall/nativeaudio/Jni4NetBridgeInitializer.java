package de.tobias.playwall.nativeaudio;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import net.sf.jni4net.Bridge;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;

public class Jni4NetBridgeInitializer
{
	private static final String[] RESOURCE_DLLS = {
			"jni4net.n-0.8.9.0.dll",
			"jni4net.n.w32.v40-0.8.9.0.dll",
			"jni4net.n.w64.v40-0.8.9.0.dll",
	};

	private static boolean loaded;

	public static void initialize() throws IOException
	{
		if(loaded)
		{
			return;
		}

		final App app = ApplicationUtils.getApplication();
		Path resourceFolder = copyResources("j4n/", RESOURCE_DLLS, "j4n", Jni4NetBridgeInitializer.class.getClassLoader());

		Bridge.setVerbose(app.isDebug());
		Bridge.init(resourceFolder.toFile());
		loaded = true;
	}

	public static void loadDll(ClassLoader classLoader, String classpathDirectory, String target, String proxyDll, String... resources) throws IOException
	{
		Path resourceFolder = copyResources(classpathDirectory, resources, target, classLoader);
		Bridge.LoadAndRegisterAssemblyFrom(resourceFolder.resolve(proxyDll).toFile(), classLoader);
	}

	private static Path copyResources(String classpathDirectory, String[] resources, String destination, ClassLoader classLoader) throws IOException
	{
		final App app = ApplicationUtils.getApplication();
		final Path resourceFolder = app.getPath(PathType.LIBRARY, destination);

		if(Files.notExists(resourceFolder))
		{
			Files.createDirectories(resourceFolder);
		}

		Arrays.stream(resources).forEach(resource -> {
			final Path dest = resourceFolder.resolve(resource);
			try
			{
				Files.copy(Objects.requireNonNull(classLoader.getResourceAsStream(classpathDirectory + resource)), dest, StandardCopyOption.REPLACE_EXISTING);
			}
			catch(IOException e)
			{
				throw new UncheckedIOException(e);
			}
		});
		return resourceFolder;
	}
}
