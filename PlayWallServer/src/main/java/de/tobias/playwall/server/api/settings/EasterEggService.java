package de.tobias.playwall.server.api.settings;

import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class EasterEggService
{
	private final AudioHandlerFactory audioHandlerFactory;

	private AudioHandler audioHandler;

	public void play() throws IOException
	{
		if(audioHandler == null)
		{
			audioHandler = audioHandlerFactory.createAudioHandler(() -> {});
			audioHandler.setOutputDevice(null);
			audioHandler.setVolume(0.5);

			try
			{
				audioHandler.loadMedia(Paths.get(getClass().getClassLoader().getResource("de/tobias/playwall/server/sound/easter-egg.mp3").toURI()));
			}
			catch(URISyntaxException e)
			{
				throw new IOException(e);
			}
		}

		if(audioHandler.isPlaying())
		{
			audioHandler.stop();
		}
		audioHandler.play();
	}
}
