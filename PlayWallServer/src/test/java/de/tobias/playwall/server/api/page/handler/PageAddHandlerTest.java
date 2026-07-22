package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.page.request.PageAddRequest;
import de.tobias.playwall.common.api.page.update.PageAddUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.page.Page;
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
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class PageAddHandlerTest extends AbstractUndoableRequestHandlerTest<PageAddRequest>
{
	@Autowired
	private ProjectController projectController;

	@MockitoSpyBean
	private ProjectService projectService;

	@Autowired
	private JsonMapper objectMapper;

	@Autowired
	private PageAddHandler handler;

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
	void testAddPageSuccessful() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		handler.handleRequest(new PageAddRequest());
		assertThat(applicationEvents.stream(PageAddUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(update -> {
					assertThat(update.getPage().id()).isNotNull();
					assertThat(update.getPage().id()).isNotNull();
					assertThat(update.getPage().settings().name()).isEqualTo("Seite 2");
					assertThat(update.getPage().settings().color()).isEqualTo(Color.GRAY1);
					assertThat(update.getPage().position()).isEqualTo(1);
					assertThat(update.getPage().pads()).hasSize(6 * 4);
				});
	}

	@Test
	void testAddPageNameCollision() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final Page page = project.getPageById(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea")).orElseThrow();
		page.getSettings().setName("Seite 2");

		projectController.loadProject(project).get();

		handler.handleRequest(new PageAddRequest());
		assertThat(applicationEvents.stream(PageAddUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(update -> {
					assertThat(update.getPage().id()).isNotNull();
					assertThat(update.getPage().settings().name()).isEqualTo("Seite 3");
					assertThat(update.getPage().position()).isEqualTo(1);
					assertThat(update.getPage().pads()).hasSize(6 * 4);
				});
	}

	@Test
	void testAddPageProjectNotLoaded() throws Exception
	{
		final PageAddRequest request = new PageAddRequest();
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotLoadedException.class);

		verify(projectService, never()).addProject(any(), anyInt(), anyInt());
	}

	@Test
	void testUndoOperation() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");

		final PageAddRequest request = new PageAddRequest();
		testInverseOperation(project, request);
	}
}