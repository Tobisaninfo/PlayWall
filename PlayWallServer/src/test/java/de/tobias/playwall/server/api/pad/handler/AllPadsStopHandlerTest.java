package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.pad.request.AllPadsStopRequest;
import de.tobias.playwall.common.api.pad.request.PadSettingsUpdateRequest;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.config.SyncAsyncConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
@Import(SyncAsyncConfig.class)
@ExtendWith(MockitoExtension.class)
class AllPadsStopHandlerTest extends AbstractUndoableRequestHandlerTest<PadSettingsUpdateRequest>
{
	private static final UUID PAD_ID = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

	@Autowired
	private ApplicationEvents applicationEvents;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private AllPadsStopHandler handler;

	@BeforeEach
	void init() throws URISyntaxException, ExecutionException, InterruptedException
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		project.getMetadata().getFadeSettings().setFadeOutDuration(2.0);
		project.getMetadata().getFadeSettings().setFadeOutOnStop(true);
		final String oldMediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_2.mp3")).toURI()).toAbsolutePath().toString().replace("\\", "/");
		project.getPad(PAD_ID).setContent(AudioPadContent.builder().mediaPath(oldMediaPath).loop(true).build());
		projectController.loadProject(project).get();
		applicationEvents.clear();
	}

	@Test
	void testAllPadStopHandlerImminently() throws Exception
	{
		projectController.getPadController(PAD_ID).play();
		assertThat(projectController.getPadController(PAD_ID).getStatus()).isNotEqualTo(PadControllerStatus.READY);

		handler.handleRequest(new AllPadsStopRequest(true));

		assertThat(projectController.getPadController(PAD_ID).getStatus()).isEqualTo(PadControllerStatus.READY);
	}

	@Test
	void testAllPadStopHandlerWithFadeOut() throws Exception
	{
		projectController.getPadController(PAD_ID).play();
		assertThat(projectController.getPadController(PAD_ID).getStatus()).isNotEqualTo(PadControllerStatus.READY);

		handler.handleRequest(new AllPadsStopRequest(false));

		assertThat(projectController.getPadController(PAD_ID).getStatus()).isEqualTo(PadControllerStatus.STOPPING);
	}
}
