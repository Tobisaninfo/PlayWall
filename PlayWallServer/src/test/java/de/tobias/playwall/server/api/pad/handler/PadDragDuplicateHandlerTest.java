package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadDragDuplicateRequest;
import de.tobias.playwall.common.api.pad.update.PadReplaceUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.page.Page;
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
class PadDragDuplicateHandlerTest extends AbstractUndoableRequestHandlerTest<PadDragDuplicateRequest>
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
	private PadDragDuplicateHandler handler;

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
				of(UUID.fromString("efb30a6f-593b-4a15-94db-faa2d4117e4f"), UUID.fromString("fc427184-2e55-4734-8148-5fb657963616")),
				of(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"), UUID.fromString("eff6cf01-20a3-4690-a6dd-395f0daf04de"))
		);
	}

	@ParameterizedTest
	@MethodSource("parameters")
	void testPadDragDuplicate(UUID padId1, UUID padId2) throws Exception
	{
		final Pad pad2 = projectController.getPad(padId2);
		final Page page2 = projectController.getPageByPad(padId2);

		final PadDragDuplicateRequest request = new PadDragDuplicateRequest(padId1, padId2);
		handler.handleRequest(request);

		final Pad pad1 = projectController.getPad(padId1);

		assertThat(project.getPad(padId2)).isNull();
		assertThat(page2.getPad(pad2.getPosition())).usingRecursiveComparison()
				.ignoringFieldsMatchingRegexes("id", "position")
				.isEqualTo(pad1);

		assertThat(applicationEvents.stream(PadReplaceUpdate.class)).hasSize(1)
				.first()
				.satisfies(update -> {
					assertThat(update.getSourcePad()).usingRecursiveComparison()
							.ignoringFieldsMatchingRegexes("id", "position")
							.isEqualTo(pad1);
					assertThat(update.getTargetPadId()).isEqualTo(padId2);
				});
	}

	@ParameterizedTest
	@MethodSource("parameters")
	void testUndoOperation(UUID padId1, UUID padId2) throws Exception
	{
		final PadDragDuplicateRequest request = new PadDragDuplicateRequest(padId1, padId2);

		testInverseOperation(project, request);
	}
}
