package de.tobias.playwall.nativeaudio.audio.rust;


import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.project.PadController;
import org.springframework.stereotype.Service;

@Service
public class NativeAudioWinHandlerFactory implements AudioHandlerFactory
{
	@Override
	public AudioHandler createAudioHandler(PadController padController)
	{
		return new NativeAudioRustHandler(padController);
	}
}
