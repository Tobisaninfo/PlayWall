package de.tobias.playwall.nativeaudio.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public interface AudioModuleLoader
{
	/**
	 * Load the native resources
	 */
	void preInit() throws IOException;

	default Path copyResource(Path resourceFolder, String packageName, String file) throws IOException
	{
		Path dest = resourceFolder.resolve(file);

		try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(packageName + file))
		{
			if(inputStream == null)
			{
				throw new IOException("Resource not found: " + packageName + file);
			}
			Files.copy(inputStream, dest, StandardCopyOption.REPLACE_EXISTING);
		}

		return dest;
	}
}
