package de.tobias.playwall.server.api.settings;

import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestSoundService
{
	private static final String TEST_SOUND_WAV = "de/tobias/playwall/server/sound/Test-Sound.wav";

	private final AudioHandlerFactory audioHandlerFactory;

	private AudioHandler audioHandler;

	public void play(String audioDeviceName) throws IOException
	{
		stop();

		audioHandler = audioHandlerFactory.createAudioHandler(() -> {});
		audioHandler.setOutputDevice(audioDeviceName);
		audioHandler.setLooping(true);

		try
		{
			audioHandler.loadMedia(Paths.get(requireNonNull(getClass().getClassLoader().getResource(TEST_SOUND_WAV)).toURI()));
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
