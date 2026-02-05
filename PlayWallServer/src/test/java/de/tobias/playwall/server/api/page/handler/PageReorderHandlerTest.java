package de.tobias.playwall.server.api.page.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.page.request.PageReorderRequest;
import de.tobias.playwall.common.api.page.update.PageReorderUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class PageReorderHandlerTest extends AbstractUndoableRequestHandlerTest<PageReorderRequest>
{
	@Autowired
	private ProjectController projectController;

	@MockitoSpyBean
	private ProjectService projectService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PageReorderHandler handler;

	@Autowired
	private ApplicationEvents applicationEvents;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@BeforeEach
	void init()
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);

		projectController.unloadProject();
	}

	@Test
	void testReorderPageSuccessful() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		projectController.loadProject(project).get();

		handler.handleRequest(new PageReorderRequest(Map.of(
				UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677"), 1,
				UUID.fromString("209e515b-4237-4fc3-968b-79da4356836d"), 0
		)));

		assertThat(project.getPages().get(0).getId()).isEqualTo(UUID.fromString("209e515b-4237-4fc3-968b-79da4356836d"));
		assertThat(project.getPages().get(1).getId()).isEqualTo(UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677"));

		assertThat(applicationEvents.stream(PageReorderUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(update -> {
					assertThat(update.getPositions()).containsExactlyInAnyOrderEntriesOf(Map.of(
							UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677"), 1,
							UUID.fromString("209e515b-4237-4fc3-968b-79da4356836d"), 0
					));
				});
	}

	@Test
	void testUndoOperation() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");

		final PageReorderRequest request = new PageReorderRequest(Map.of(
				UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677"), 1,
				UUID.fromString("209e515b-4237-4fc3-968b-79da4356836d"), 0
		));
		testInverseOperation(project, handler, request);
	}
}