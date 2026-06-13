package de.tobias.playwall.nativeaudio.audio.rust;

import de.tobias.playwall.nativeaudio.extensions.RustAudioLoaderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SuppressWarnings("java:S2925")
@ExtendWith(RustAudioLoaderExtension.class)
@ExtendWith(MockitoExtension.class)
class RustAudioHandlerTest
{
	final Runnable eofCallback = Mockito.mock(Runnable.class);

	@TempDir
	private Path tempDir;

	@Test
	void testLoadMediaNormal() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();
	}

	@Test
	void testLoadMediaNotFound()
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		final Path path = Paths.get("/tmp/demo.mp3");
		assertThatThrownBy(() -> handler.loadMedia(path)).isInstanceOf(FileNotFoundException.class);
		assertThat(handler.isMediaLoaded()).isFalse();
	}

	@Test
	void testLoadMediaCorruptedData() throws IOException
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		final Path path = new ClassPathResource("audio/corrupted.mp3").getFile().toPath();
		assertThatThrownBy(() -> handler.loadMedia(path)).isInstanceOf(IOException.class);
		assertThat(handler.isMediaLoaded()).isFalse();
	}

	@Test
	void testPlay() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);
		handler.play();

		assertThat(handler.isPlaying()).isTrue();
	}

	@Test
	void testPlayFileNotFound() throws IOException
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		final Path source = new ClassPathResource("audio/example_1.mp3").getFile().toPath();
		final Path target = tempDir.resolve("example_1.mp3");
		Files.copy(source, target);

		handler.loadMedia(target);
		assertThat(handler.isMediaLoaded()).isTrue();

		Files.delete(target);

		assertThatThrownBy(handler::play).isInstanceOf(FileNotFoundException.class);
	}

	@Test
	void testPauseOnPlayingAudio() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);
		handler.play();
		assertThat(handler.isPlaying()).isTrue();

		Thread.sleep(100);

		handler.pause();
		assertThat(handler.isPlaying()).isFalse();
	}

	@Test
	void testPauseOnStoppedAudio() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);

		handler.pause();
		assertThat(handler.isPlaying()).isFalse();
	}

	@Test
	void testStopOnPlayingAudio() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);
		handler.play();
		assertThat(handler.isPlaying()).isTrue();

		Thread.sleep(100);

		handler.stop();
		assertThat(handler.isPlaying()).isFalse();
	}

	@Test
	void testStopOnStoppedAudio() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);

		handler.stop();
		assertThat(handler.isPlaying()).isFalse();
	}

	@Test
	void testEof() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		handler.loadMedia(new ClassPathResource("audio/example_2.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);
		handler.play();

		await()
				.atMost(2500, MILLISECONDS)
				.untilAsserted(() -> verify(eofCallback).run());
	}

	@Test
	void testGetDuration() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		assertThat(handler.getDuration().toSeconds()).isEqualTo(30);
	}

	@Test
	void testUnloadMedia() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(eofCallback);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.unloadMedia();
		assertThat(handler.isMediaLoaded()).isFalse();
	}
}
