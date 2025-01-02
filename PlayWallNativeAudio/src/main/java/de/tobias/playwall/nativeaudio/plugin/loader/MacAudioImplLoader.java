package de.tobias.playwall.nativeaudio.plugin.loader;

import de.tobias.playwall.nativeaudio.audio.mac.NativeAudioMacHandlerFactory;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MacAudioImplLoader implements AudioModuleLoader
{
	private static final String ASSETS = "mac/";
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
				Path dest = copyResource(resourceFolder, ASSETS, "libNativeAudio.dylib");
				System.load(dest.toString());
				loaded = true;
			}
		}
		catch(IOException e)
		{
			throw new RuntimeException("Failed to pre-initialize MacAudioImplLoader", e);
		}
	}

	@Override
	public AudioHandlerFactory init()
	{
		return new NativeAudioMacHandlerFactory("NativeAudio");
	}
}

