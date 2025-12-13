package de.tobias.playwall.nativeaudio.audio.rust;

import de.tobias.playwall.nativeaudio.exitensions.RustAudioLoaderExtension;
import de.tobias.playwall.server.common.project.PadController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

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
		handler.play();

		assertThat(handler.isPlaying()).isTrue();
	}

	@Test
	void testPauseOnPlayingAudio() throws Exception
	{
		final RustAudioHandler handler = new RustAudioHandler(mock);
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
		handler.play();
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
}
