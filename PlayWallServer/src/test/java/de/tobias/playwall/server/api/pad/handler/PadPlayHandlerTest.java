package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadPlayRequest;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.api.pad.PadNotExistsException;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PadPlayHandlerTest extends AbstractRequestHandlerTest
{
	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private JsonMapper objectMapper;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private PadPlayHandler handler;

	private final AudioHandler audioHandler = mock(AudioHandler.class);

	private static final UUID PAD_ID = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
	private static final UUID SECOND_PAD_ID = UUID.fromString("fc427184-2e55-4734-8148-5fb657963617");

	@BeforeEach
	void init()
	{
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);
	}

	@Test
	void testPadPlayHandlerOnExistingPad() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString().replace("\\", "/");
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());
		projectController.loadProject(project).get();

		handler.handleRequest(new PadPlayRequest(padId));

		verify(audioHandler).play();
	}

	@Test
	void testPadPlayHandlerOnNotExistingPad() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8448-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final PadPlayRequest request = new PadPlayRequest(padId);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(PadNotExistsException.class);

		verify(audioHandler, never()).play();
	}


	@Test
	void testPadPlayHandlerInSoloModeStopsCurrentlyPlayingPads() throws Exception
	{
		loadProjectWithTwoPads(true);

		handler.handleRequest(new PadPlayRequest(PAD_ID));
		handler.handleRequest(new PadPlayRequest(SECOND_PAD_ID));

		verify(audioHandler, times(2)).play();
		verify(audioHandler).stop();

		assertThat(projectController.getPlayingPadControllers())
				.extracting(PadController::getPad)
				.extracting(Pad::getId)
				.containsExactly(SECOND_PAD_ID);
	}

	@Test
	void testPadPlayHandlerInMultiModeDoesNotStopCurrentlyPlayingPads() throws Exception
	{
		loadProjectWithTwoPads(false);

		handler.handleRequest(new PadPlayRequest(PAD_ID));
		handler.handleRequest(new PadPlayRequest(SECOND_PAD_ID));

		verify(audioHandler, times(2)).play();
		verify(audioHandler, never()).stop();
	}

	@Test
	void testPadPlayHandlerInSoloModeDoesNotStopPadsWithIgnoreSoloMode() throws Exception
	{
		loadProjectWithTwoPads(true, true);

		handler.handleRequest(new PadPlayRequest(SECOND_PAD_ID));
		handler.handleRequest(new PadPlayRequest(PAD_ID));

		verify(audioHandler, times(2)).play();
		verify(audioHandler, never()).stop();

		assertThat(projectController.getPlayingPadControllers())
				.extracting(PadController::getPad)
				.extracting(Pad::getId)
				.containsExactlyInAnyOrder(PAD_ID, SECOND_PAD_ID);
	}

	@Test
	void testPadPlayHandlerInSoloModeDoesNotStopRetriggeredPadWithIgnoreSoloMode() throws Exception
	{
		loadProjectWithTwoPads(true, true);

		handler.handleRequest(new PadPlayRequest(SECOND_PAD_ID));
		handler.handleRequest(new PadPlayRequest(SECOND_PAD_ID));

		verify(audioHandler, times(2)).play();
		verify(audioHandler, never()).stop();
	}

	private void loadProjectWithTwoPads(boolean isSoloMode) throws URISyntaxException, ExecutionException, InterruptedException
	{
		loadProjectWithTwoPads(isSoloMode, false);
	}

	private void loadProjectWithTwoPads(boolean isSoloMode, boolean secondPadIgnoresSoloMode) throws URISyntaxException, ExecutionException, InterruptedException
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		project.getMetadata().setIsSoloMode(isSoloMode);

		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();
		project.getPad(PAD_ID).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());

		final Pad secondPad = Pad.builder()
				.id(SECOND_PAD_ID)
				.name("Second Pad")
				.position(1)
				.content(AudioPadContent.builder().mediaPath(mediaPath).loop(false).ignoreSoloMode(secondPadIgnoresSoloMode).build())
				.build();
		project.getPageByPad(PAD_ID).getPads().add(secondPad);

		projectController.loadProject(project).get();
	}

}
