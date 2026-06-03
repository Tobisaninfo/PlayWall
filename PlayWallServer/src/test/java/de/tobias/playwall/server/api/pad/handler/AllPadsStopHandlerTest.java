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

import java.nio.file.Paths;
import java.util.UUID;

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
	@Autowired
	private ApplicationEvents applicationEvents;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private AllPadsStopHandler handler;

	@BeforeEach
	void init()
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);
	}

	@Test
	void testAllPadStopHandler() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final String oldMediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_2.mp3")).toURI()).toAbsolutePath().toString();
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(oldMediaPath).loop(true).build());
		projectController.loadProject(project).get();
		applicationEvents.clear();

		projectController.getPadController(padId).play();
		assertThat(projectController.getPadController(padId).getStatus()).isNotEqualTo(PadControllerStatus.READY);

		handler.handleRequest(new AllPadsStopRequest());

		assertThat(projectController.getPadController(padId).getStatus()).isEqualTo(PadControllerStatus.READY);
	}
}
