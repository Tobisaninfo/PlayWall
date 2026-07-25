package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadDragSwapRequest;
import de.tobias.playwall.common.api.pad.update.PadSwapUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.config.SyncAsyncConfig;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
@Import(SyncAsyncConfig.class)
@ExtendWith(MockitoExtension.class)
class PadDragSwapHandlerTest extends AbstractUndoableRequestHandlerTest<PadDragSwapRequest>
{
	@Autowired
	private ApplicationEvents applicationEvents;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Mock
	private AudioHandler audioHandler;

	@Autowired
	private JsonMapper objectMapper;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private PadDragSwapHandler handler;

	private Project project;

	@BeforeEach
	void init() throws ExecutionException, InterruptedException
	{
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);

		project = TestUtils.loadProject(objectMapper, "projects/project_8.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();
	}

	private static Stream<Arguments> parameters()
	{
		return Stream.of(
				of(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"), UUID.fromString("efb30a6f-593b-4a15-94db-faa2d4117e4f")),
				of(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"), UUID.fromString("eff6cf01-20a3-4690-a6dd-395f0daf04de"))
		);
	}

	@ParameterizedTest
	@MethodSource("parameters")
	void testPadDragSwap(UUID padId1, UUID padId2) throws Exception
	{
		final int pad1Position = projectController.getPad(padId1).getPosition();
		final int pad2Position = projectController.getPad(padId2).getPosition();

		final PadDragSwapRequest request = new PadDragSwapRequest(padId1, padId2);
		handler.handleRequest(request);

		assertThat(project.getPad(padId2).getPosition()).isEqualTo(pad1Position);
		assertThat(project.getPad(padId1).getPosition()).isEqualTo(pad2Position);

		assertThat(applicationEvents.stream(PadSwapUpdate.class)).hasSize(1)
				.first()
				.satisfies(update -> {
					assertThat(update.getPad1()).isEqualTo(padId1);
					assertThat(update.getPad2()).isEqualTo(padId2);
				});
	}

	@ParameterizedTest
	@MethodSource("parameters")
	void testUndoOperation(UUID padId1, UUID padId2) throws Exception
	{
		final PadDragSwapRequest request = new PadDragSwapRequest(padId1, padId2);

		testInverseOperation(project, request);
	}
}
