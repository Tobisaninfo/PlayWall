package de.tobias.playwall.server.audio;

import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.nativeaudio.loader.AudioModuleLoader;
import de.tobias.playwall.nativeaudio.loader.RustAudioImplLoader;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.project.PadController;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@AllArgsConstructor
@Service
public class PlatformAudioHandlerFactory
{
	private final AudioHandlerFactory audioHandlerFactory;

	@PostConstruct
	private void loadNativeLibraries() throws IOException
	{
		final AudioModuleLoader loader = switch(OS.getType())
		{
			case Windows, MacOSX -> new RustAudioImplLoader();
			default -> throw new IllegalArgumentException("Unsupported OS type " + OS.getType());
		};

		loader.preInit();
	}

	public AudioHandler createAudioHandler(PadController padController)
	{
		return audioHandlerFactory.createAudioHandler(padController);
	}
}
