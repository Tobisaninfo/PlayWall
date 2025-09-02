package de.tobias.playwall.nativeaudio.loader;

import de.tobias.playwall.nativeaudio.audio.rust.NativeAudioRustHandler;
import de.tobias.playwall.nativeaudio.audio.rust.RustLogLevel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RustAudioImplLoader implements AudioModuleLoader
{
	private static final String ASSETS = "rust/";
	private boolean loaded = false;

	@Override
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
				NativeAudioRustHandler.initSystem(RustLogLevel.DEBUG); // TODO: make configurable
				loaded = true;
			}
		}
		catch(IOException e)
		{
			throw new UncheckedIOException("Failed to pre-initialize MacAudioImplLoader", e);
		}
	}
}

