package de.tobias.playwall.nativeaudio.loader;

import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.nativeaudio.audio.rust.RustAudioHandler;
import de.tobias.playwall.nativeaudio.audio.rust.RustLogLevel;
import de.tobias.playwall.server.common.storage.PathProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Profile("!test")
public class RustAudioLoader
{
	private boolean isLoaded = false;

	private final PathProvider pathProvider;

	@PostConstruct
	void preInit()
	{
		final String nativeLibraryFilename = OS.isWindows() ? "PlayWallNativeAudioRust.dll" : "libPlayWallNativeAudioRust.dylib";
		final Path destinationPath = pathProvider.getPathForNativeLibrary(nativeLibraryFilename);

		try
		{
			if(Files.notExists(destinationPath.getParent()))
			{
				Files.createDirectories(destinationPath.getParent());
			}

			if(!isLoaded)
			{
				copyResource("rust/" + nativeLibraryFilename, destinationPath);
				System.load(destinationPath.toString());
				RustAudioHandler.initSystem(RustLogLevel.DEBUG); // TODO: make configurable
				isLoaded = true;
			}
		}
		catch(IOException e)
		{
			throw new UncheckedIOException("Failed to pre-initialize RustAudioImplLoader", e);
		}
	}

	private void copyResource(String source, Path destination) throws IOException
	{
		try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(source))
		{
			if(inputStream == null)
			{
				throw new IOException("Resource not found: " + source);
			}
			Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}

