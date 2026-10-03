package de.tobias.playwall.client.domain.companion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CompanionPluginExporter
{
	public static final String RESOURCE_PATH = "companion/playwall.tgz";
	public static final String FILE_NAME = "playwall.tgz";

	private final ClassLoader classLoader;

	public CompanionPluginExporter()
	{
		this(CompanionPluginExporter.class.getClassLoader());
	}

	CompanionPluginExporter(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

	public boolean isPluginAvailable()
	{
		return classLoader.getResource(RESOURCE_PATH) != null;
	}

	public Path export(Path targetFolder) throws IOException
	{
		try(InputStream inputStream = classLoader.getResourceAsStream(RESOURCE_PATH))
		{
			if(inputStream == null)
			{
				throw new IOException("Companion plugin is not bundled with this build");
			}
			final Path target = targetFolder.resolve(FILE_NAME);
			Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
			return target;
		}
	}
}
