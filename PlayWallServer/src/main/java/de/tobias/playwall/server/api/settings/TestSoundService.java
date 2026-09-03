package de.tobias.playwall.server.api.settings;

import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.storage.PathProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestSoundService
{
	private static final String TEST_SOUND_WAV = "de/tobias/playwall/server/sound/Test-Sound.wav";

	private final PathProvider pathProvider;
	private final AudioHandlerFactory audioHandlerFactory;

	private AudioHandler audioHandler;


	private Path extractTestSoundToTempFile() throws IOException
	{
		final Path tempFile = pathProvider.getPathForTemp().resolve("playwall-test-sound.wav");
		if(Files.exists(tempFile))
		{
			return tempFile;
		}

		Files.createDirectories(tempFile.getParent());

		try(InputStream inputStream = requireNonNull(getClass().getClassLoader().getResourceAsStream(TEST_SOUND_WAV)))
		{
			Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
		}

		return tempFile;
	}

	public void play(String audioDeviceName) throws IOException
	{
		stop();

		audioHandler = audioHandlerFactory.createAudioHandler(() -> {});
		audioHandler.setOutputDevice(audioDeviceName, false);
		audioHandler.setLooping(true);

		audioHandler.loadMedia(extractTestSoundToTempFile());
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
