package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.page.request.PageSettingsUpdateRequest;
import de.tobias.playwall.common.api.page.update.PageSettingsUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.page.PageNameAlreadyExistsException;
import de.tobias.playwall.server.api.page.PageNotExistsException;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class PageSettingsUpdateHandlerTest extends AbstractUndoableRequestHandlerTest<PageSettingsUpdateRequest>
{
	@Autowired
	private ProjectController projectController;

	@MockitoSpyBean
	private ProjectService projectService;

	@Autowired
	private JsonMapper objectMapper;

	@Autowired
	private PageSettingsUpdateHandler handler;

	@Autowired
	private ApplicationEvents applicationEvents;

	@BeforeEach
	void init()
	{
		projectController.unloadProject();
	}

	@Test
	void testRenamePageSuccessful() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final UUID pageId = UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea");
		handler.handleRequest(new PageSettingsUpdateRequest(pageId, "Renamed Page", Color.RED1));

		assertThat(applicationEvents.stream(PageSettingsUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(update -> {
					assertThat(update.getPageId()).isEqualTo(pageId);
					assertThat(update.getName()).isEqualTo("Renamed Page");
					assertThat(update.getColor()).isEqualTo(Color.RED1);
				});

		assertThat(project.getPageById(pageId).orElseThrow().getName()).isEqualTo("Renamed Page");
	}

	@Test
	void testRenamePagePageDuplicatedName() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		projectController.loadProject(project).get();

		final UUID pageId = UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677");
		final PageSettingsUpdateRequest request = new PageSettingsUpdateRequest(pageId, "Seite 3", Color.GRAY1);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(PageNameAlreadyExistsException.class);
	}

	@Test
	void testRenamePagePageNotExists() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final UUID pageId = UUID.fromString("1e76b8b3-2da8-4533-aa57-e2b66360e9ea");
		final PageSettingsUpdateRequest request = new PageSettingsUpdateRequest(pageId, "Renamed Page", Color.GRAY1);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(PageNotExistsException.class);
	}

	@Test
	void testRenamePageProjectNotLoaded()
	{
		final UUID pageId = UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea");
		final PageSettingsUpdateRequest request = new PageSettingsUpdateRequest(pageId, "Renamed Page", Color.GRAY1);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotLoadedException.class);

		verify(projectService, never()).renamePage(any(), any(), any());
	}

	@Test
	void testUndoOperation() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");

		final UUID pageId = UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea");
		final PageSettingsUpdateRequest request = new PageSettingsUpdateRequest(pageId, "Renamed Page", Color.GRAY1);

		testInverseOperation(project, request);
	}
}