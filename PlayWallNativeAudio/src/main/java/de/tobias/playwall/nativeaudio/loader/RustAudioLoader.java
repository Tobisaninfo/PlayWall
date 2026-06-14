package de.tobias.playwall.nativeaudio.loader;

import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.nativeaudio.NativeAudioProperties;
import de.tobias.playwall.nativeaudio.audio.rust.RustAudioHandler;
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
	private final NativeAudioProperties nativeAudioProperties;

	@PostConstruct
	void preInit()
	{
		final String nativeLibraryFilename = switch(OS.getType())
		{
			case Windows -> "PlayWallNativeAudioRust.dll";
			case MacOSX -> "libPlayWallNativeAudioRust.dylib";
			case Linux -> "libPlayWallNativeAudioRust.so";
			case Other -> throw new UnsupportedOperationException();
		};
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
				RustAudioHandler.initSystem(nativeAudioProperties.getLogLevel());
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

