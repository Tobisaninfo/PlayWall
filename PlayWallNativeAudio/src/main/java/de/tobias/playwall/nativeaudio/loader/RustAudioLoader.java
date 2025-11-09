package de.tobias.playwall.nativeaudio.loader;

import de.tobias.playwall.nativeaudio.audio.rust.RustAudioHandler;
import de.tobias.playwall.nativeaudio.audio.rust.RustLogLevel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Profile("!test")
public class RustAudioLoader implements AudioModuleLoader
{
	private static final String ASSETS = "rust/";
	private boolean loaded = false;

	@Override
	@PostConstruct
	public void preInit()
	{
		// TODO
		Path resourceFolder = Paths.get(System.getProperty("user.home"));

		try
		{
			if(Files.notExists(resourceFolder))
			{
				Files.createDirectories(resourceFolder);
			}

			if(!loaded)
			{
				Path dest = copyResource(resourceFolder, ASSETS, "libPlayWallNativeAudioRust.dylib");
				System.load(dest.toString());
				RustAudioHandler.initSystem(RustLogLevel.DEBUG); // TODO: make configurable
				loaded = true;
			}
		}
		catch(IOException e)
		{
			throw new UncheckedIOException("Failed to pre-initialize RustAudioImplLoader", e);
		}
	}
}

