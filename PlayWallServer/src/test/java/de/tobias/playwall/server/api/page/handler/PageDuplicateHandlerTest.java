package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageDuplicateRequest;
import de.tobias.playwall.common.api.page.PageDto;
import de.tobias.playwall.common.api.page.request.PageAddResponse;
import de.tobias.playwall.common.api.page.request.PageNotExistsError;
import de.tobias.playwall.common.api.page.update.PageInsertUpdate;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.pad.PadMapper;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class PageDuplicateHandlerTest extends AbstractUndoableRequestHandlerTest<PageDuplicateRequest>
{
	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private ProjectController projectController;

	@MockitoSpyBean
	private ProjectService projectService;

	@Autowired
	private JsonMapper objectMapper;

	@Autowired
	private PageDuplicateHandler handler;

	@Autowired
	private PadMapper padMapper;

	@BeforeEach
	void init()
	{
		projectController.unloadProject();
	}

	@Test
	void testDuplicatePageSuccessful() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		projectController.loadProject(project).get();

		final UUID originalPage = UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677");
		handler.handleRequest(new PageDuplicateRequest(originalPage));

		final Page originalPageInstance = project.getPageById(originalPage).orElseThrow();
		assertThat(applicationEvents.stream(PageInsertUpdate.class))
				.hasSize(1)
				.last()
				.satisfies(event -> {
					assertThat(event.getPage().id()).isNotEqualTo(originalPageInstance.getId());
					assertThat(event.getPage().name()).isEqualTo(originalPageInstance.getName() + " - 1");
					assertThat(event.getPage().position()).isEqualTo(originalPageInstance.getPosition() + 1);
					assertThat(event.getPage().pads()).extracting(padMapper::padDtoToPad).usingRecursiveComparison()
							.ignoringFieldsMatchingRegexes(".*id")
							.isEqualTo(originalPageInstance.getPads());

					assertThat(event.getIndex()).isEqualTo(originalPageInstance.getPosition() + 1);
					assertThat(event.getPositions()).containsExactlyInAnyOrderEntriesOf(Map.of(originalPage, 0,
							event.getPage().id(), 1,
							UUID.fromString("209e515b-4237-4fc3-968b-79da4356836d"), 2
					));
				});
	}

	@Test
	void testDuplicatePagePageNotExists() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		projectController.loadProject(project).get();

		final UUID originalPage = UUID.fromString("1e76b8b3-2da8-4533-aa57-e2b66360e9ea");
		assertThatThrownBy(() -> handler.handleRequest(new PageDuplicateRequest(originalPage)))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(PageNotExistsError.class);
	}

	@Test
	void testDuplicatePageProjectNotLoaded() throws Exception
	{
		final UUID originalPage = UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea");
		assertThatThrownBy(() -> handler.handleRequest(new PageDuplicateRequest(originalPage)))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(ProjectNotLoadedError.class);

		verify(projectService, never()).duplicatePage(any(), any());
	}

	@Test
	void testUndoOperation() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");

		final PageDuplicateRequest request = new PageDuplicateRequest(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"));
		testInverseOperation(project, request);
	}
}