package de.tobias.playwall.server.api.page.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.page.request.PageAddRequest;
import de.tobias.playwall.common.api.page.update.PageAddUpdate;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class PageAddHandlerTest
{
	@Autowired
	private ProjectController projectController;

	@MockitoSpyBean
	private ProjectService projectService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PageAddHandler handler;

	@Autowired
	private ApplicationEvents applicationEvents;

	@BeforeEach
	void init()
	{
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
					assertThat(update.getPage().name()).isEqualTo("Seite 2");
					assertThat(update.getPage().position()).isEqualTo(1);
					assertThat(update.getPage().pads()).hasSize(6 * 4);
				});
	}

	@Test
	void testAddPageNameCollision() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final Page page = project.getPageById(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea")).orElseThrow();
		page.setName("Seite 2");

		projectController.loadProject(project).get();

		handler.handleRequest(new PageAddRequest());
		assertThat(applicationEvents.stream(PageAddUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(update -> {
					assertThat(update.getPage().id()).isNotNull();
					assertThat(update.getPage().name()).isEqualTo("Seite 3");
					assertThat(update.getPage().position()).isEqualTo(1);
					assertThat(update.getPage().pads()).hasSize(6 * 4);
				});
	}

	@Test
	void testAddPageProjectNotLoaded() throws Exception
	{
		assertThatThrownBy(() -> handler.handleRequest(new PageAddRequest()))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(ProjectNotLoadedError.class);

		verify(projectService, never()).addProject(any(), anyInt(), anyInt());
	}

	// TODO: Undo test after implementing PW-56
}