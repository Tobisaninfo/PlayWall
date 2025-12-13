package de.tobias.playwall.nativeaudio.exitensions;

import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.nativeaudio.audio.rust.RustAudioHandler;
import de.tobias.playwall.nativeaudio.audio.rust.RustLogLevel;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class RustAudioLoaderExtension implements BeforeAllCallback
{
	private static final AtomicBoolean isLoaded = new AtomicBoolean(false);
	private final String nativeLibraryFilename = OS.isWindows() ? "PlayWallNativeAudioRust.dll" : "libPlayWallNativeAudioRust.dylib";

	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
		synchronized(isLoaded)
		{
			if(!isLoaded.getAndSet(true))
			{
				final Path path = new ClassPathResource("rust/" + nativeLibraryFilename).getFile().toPath().toAbsolutePath();
				System.load(path.toString());
				RustAudioHandler.initSystem(RustLogLevel.DEBUG);
			}
		}
	}
}
