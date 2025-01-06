package de.tobias.playwall.server.audio;

import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.nativeaudio.audio.mac.NativeAudioMacHandlerFactory;
import de.tobias.playwall.nativeaudio.audio.windows.NativeAudioWinHandlerFactory;
import de.tobias.playwall.nativeaudio.loader.AudioModuleLoader;
import de.tobias.playwall.nativeaudio.loader.MacAudioImplLoader;
import de.tobias.playwall.nativeaudio.loader.WindowsAudioImplLoader;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.project.PadController;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AudioHandlerFactory
{
	@PostConstruct
	private void loadNativeLibraries() throws IOException
	{
		final AudioModuleLoader loader = switch(OS.getType())
		{
			case Windows -> new WindowsAudioImplLoader();
			case MacOSX -> new MacAudioImplLoader();
			default -> throw new IllegalArgumentException("Unsupported OS type " + OS.getType());
		};

		loader.preInit();
	}

	public AudioHandler createAudioHandler(PadController padController)
	{
		return switch(OS.getType())
		{
			case Windows -> new NativeAudioWinHandlerFactory().createAudioHandler(padController);
			case MacOSX -> new NativeAudioMacHandlerFactory().createAudioHandler(padController);
			default -> throw new IllegalArgumentException("Unsupported OS type " + OS.getType());
		};
	}
}
