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
public class TestSoundService
{
	private final AudioHandlerFactory audioHandlerFactory;

	private AudioHandler audioHandler;

	public void play(String audioDeviceName) throws IOException
	{
		stop();

		audioHandler = audioHandlerFactory.createAudioHandler(() -> {});
		audioHandler.setOutputDevice(audioDeviceName);
		try
		{
			audioHandler.loadMedia(Paths.get(getClass().getClassLoader().getResource("de/tobias/playwall/server/sound/Test-Sound.wav").toURI()));
		}
		catch(URISyntaxException e)
		{
			throw new IOException(e);
		}
		audioHandler.play();
	}

	public void stop()
	{
		if(audioHandler != null)
		{
			audioHandler.stop();
			audioHandler.unloadMedia();
		}
	}
}
