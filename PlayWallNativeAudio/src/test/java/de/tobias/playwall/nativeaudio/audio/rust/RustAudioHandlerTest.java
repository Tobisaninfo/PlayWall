package de.tobias.playwall.nativeaudio.audio.rust;

import de.tobias.playwall.nativeaudio.extensions.RustAudioLoaderExtension;
import de.tobias.playwall.server.common.project.PadController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SuppressWarnings("java:S2925")
@ExtendWith(RustAudioLoaderExtension.class)
class RustAudioHandlerTest
{
	final PadController mock = Mockito.mock(PadController.class);

	@Test
	void testPlay() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(mock);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);
		handler.play(false);

		assertThat(handler.isPlaying()).isTrue();
	}

	@Test
	void testPauseOnPlayingAudio() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(mock);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);
		handler.play(false);
		assertThat(handler.isPlaying()).isTrue();

		Thread.sleep(100);

		handler.pause();
		assertThat(handler.isPlaying()).isFalse();
	}

	@Test
	void testPauseOnStoppedAudio() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(mock);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);

		handler.pause();
		assertThat(handler.isPlaying()).isFalse();
	}

	@Test
	void testStopOnPlayingAudio() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(mock);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);
		handler.play(false);
		assertThat(handler.isPlaying()).isTrue();

		Thread.sleep(100);

		handler.stop();
		assertThat(handler.isPlaying()).isFalse();
	}

	@Test
	void testStopOnStoppedAudio() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(mock);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);

		handler.stop();
		assertThat(handler.isPlaying()).isFalse();
	}

	@Test
	void testEof() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(mock);
		handler.loadMedia(new ClassPathResource("audio/example_2.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.setVolume(0);
		handler.play(false);

		await()
				.atMost(2500, MILLISECONDS)
				.untilAsserted(() -> verify(mock).onEof());
	}

	@Test
	void testGetDuration() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(mock);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		assertThat(handler.getDuration().toSeconds()).isEqualTo(30);
	}

	@Test
	void testUnloadMedia() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(mock);
		handler.loadMedia(new ClassPathResource("audio/example_1.mp3").getFile().toPath());
		assertThat(handler.isMediaLoaded()).isTrue();

		handler.unloadMedia();
		assertThat(handler.isMediaLoaded()).isFalse();
	}
}
