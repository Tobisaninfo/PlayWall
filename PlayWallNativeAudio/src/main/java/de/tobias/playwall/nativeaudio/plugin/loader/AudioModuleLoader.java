package de.tobias.playwall.nativeaudio.plugin.loader;

import de.tobias.playwall.server.common.audio.AudioHandlerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface AudioModuleLoader
{
	/**
	 * Load the native resources
	 */
	void preInit() throws IOException;

	/**
	 * Init the audio interface
	 */
	AudioHandlerFactory init();

	default Path copyResource(Path resourceFolder, String packageName, String file) throws IOException
	{
		Path dest = resourceFolder.resolve(file);

		try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(packageName + file))
		{
			if(inputStream == null)
			{
				throw new IOException("Resource not found: " + packageName + file);
			}
			Files.copy(inputStream, dest);
		}

		return dest;
	}
}
