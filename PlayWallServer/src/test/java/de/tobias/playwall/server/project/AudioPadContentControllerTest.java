package de.tobias.playwall.server.project;

import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.project.FadeSettings;
import de.tobias.playwall.server.common.model.project.Project;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AudioPadContentControllerTest
{
	private static final UUID PAD_ID = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

	private static final ConditionFactory AWAIT = await()
			.atLeast(1, SECONDS)
			.atMost(2, SECONDS);

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private ApplicationContext context;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Mock
	private AudioHandler audioHandler;

	private Pad pad;
	private FadeSettings projectFadeSettings;
	private AudioPadContentController controller;

	@BeforeEach
	void init() throws URISyntaxException
	{
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);

		final Project project = TestUtils.loadProject(jsonMapper, "projects/project_1.json");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();

		pad = project.getPad(PAD_ID);
		pad.setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());
		controller = Mockito.spy(new AudioPadContentController(context, pad, (AudioPadContent) pad.getContent(), audioHandlerFactory, project));

		projectFadeSettings = project.getMetadata().getFadeSettings();
		projectFadeSettings.setFadeInDuration(1.0);
		projectFadeSettings.setFadeOutDuration(1.0);
	}

	@Test
	void testPlayWithoutFadeIn() throws IOException
	{
		controller.play(true);

		verify(controller, never()).fadeIn(anyDouble());
		verify(audioHandler).play();
	}

	@Test
	void testPlayWithProjectFadeIn() throws IOException
	{
		projectFadeSettings.setFadeInOnPlay(true);

		controller.play(true);

		verify(controller).fadeIn(anyDouble());
		verify(audioHandler).play();
	}

	@Test
	void testPlayWithPadFadeIn() throws IOException
	{
		pad.setFadeSettings(FadeSettings.builder()
				.fadeInDuration(1.0)
				.fadeInOnPlay(true)
				.build());

		controller.play(true);

		verify(controller).fadeIn(anyDouble());
		verify(audioHandler).play();
	}

	@Test
	void testPauseWithoutFadeOut()
	{
		controller.pause();

		verify(controller, never()).fadeOut(anyDouble(), any());
		verify(audioHandler).pause();
	}

	@Test
	void testPauseWithProjectFadeOut()
	{
		projectFadeSettings.setFadeOutOnPause(true);

		controller.pause();

		verify(controller).fadeOut(anyDouble(), any());
		AWAIT.untilAsserted(() -> verify(audioHandler).pause());
	}

	@Test
	void testPauseWithPadFadeOut()
	{
		pad.setFadeSettings(FadeSettings.builder()
				.fadeOutDuration(1.0)
				.fadeOutOnPause(true)
				.build());

		controller.pause();

		verify(controller).fadeOut(anyDouble(), any());
		AWAIT.untilAsserted(() -> verify(audioHandler).pause());
	}

	@Test
	void testResumeWithoutFadeIn() throws IOException
	{
		controller.pause();
		controller.play(true);

		verify(controller, never()).fadeIn(anyDouble());
		verify(audioHandler).play();
	}

	@Test
	void testResumeWithProjectFadeIn() throws IOException
	{
		projectFadeSettings.setFadeInOnResume(true);

		controller.pause();
		controller.play(true);

		verify(controller).fadeIn(anyDouble());
		verify(audioHandler).play();
	}

	@Test
	void testResumeWithPadFadeIn() throws IOException
	{
		pad.setFadeSettings(FadeSettings.builder()
				.fadeInDuration(1.0)
				.fadeInOnResume(true)
				.build());

		controller.pause();
		controller.play(true);

		verify(controller).fadeIn(anyDouble());
		verify(audioHandler).play();
	}

	@Test
	void testStopWithoutFadeOut()
	{
		controller.stop();

		verify(controller, never()).fadeOut(anyDouble(), any());
		verify(audioHandler).stop();
	}

	@Test
	void testStopWithProjectFadeOut()
	{
		projectFadeSettings.setFadeOutOnStop(true);

		controller.stop();

		verify(controller).fadeOut(anyDouble(), any());
		AWAIT.untilAsserted(() -> verify(audioHandler).stop());
	}

	@Test
	void testStopWithPadFadeOut()
	{
		pad.setFadeSettings(FadeSettings.builder()
				.fadeOutDuration(1.0)
				.fadeOutOnStop(true)
				.build());

		controller.stop();

		verify(controller).fadeOut(anyDouble(), any());
		AWAIT.untilAsserted(() -> verify(audioHandler).stop());
	}
}
