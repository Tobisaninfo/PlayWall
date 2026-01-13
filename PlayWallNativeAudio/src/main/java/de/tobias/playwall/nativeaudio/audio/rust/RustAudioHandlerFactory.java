package de.tobias.playwall.nativeaudio.audio.rust;


import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import org.springframework.stereotype.Service;

@Service
public class RustAudioHandlerFactory implements AudioHandlerFactory
{
	@Override
	public AudioHandler createAudioHandler(Runnable eofCallback)
	{
		return new RustAudioHandler(eofCallback);
	}
}
