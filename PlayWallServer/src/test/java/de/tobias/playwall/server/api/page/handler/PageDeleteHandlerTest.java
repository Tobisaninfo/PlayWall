package de.tobias.playwall.server.api.page.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.page.request.PageDeleteRequest;
import de.tobias.playwall.common.api.page.request.PageNotExistsError;
import de.tobias.playwall.common.api.page.update.PageAddUpdate;
import de.tobias.playwall.common.api.page.update.PageDeleteUpdate;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
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

import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class PageDeleteHandlerTest extends AbstractUndoableRequestHandlerTest<PageDeleteRequest>
{
	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private ProjectController projectController;

	@MockitoSpyBean
	private ProjectService projectService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PageDeleteHandler handler;

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
	void testDeletePageSomePage() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		projectController.loadProject(project).get();

		final UUID pageId = UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677");
		final Page page = project.getPageById(pageId).orElseThrow();

		handler.handleRequest(new PageDeleteRequest(pageId));

		assertThat(project.getPageById(pageId)).isEmpty();
		assertThat(page.getPads())
				.isNotEmpty()
				.allSatisfy(pad -> assertThat(projectController.getPadController(pad.getId())).isNull());
		assertThat(applicationEvents.stream(PageDeleteUpdate.class))
				.hasSize(1)
				.last()
				.satisfies(event -> {
					assertThat(event.getPageId()).isEqualTo(pageId);
					assertThat(event.getPositions()).containsExactly(Map.entry(UUID.fromString("209e515b-4237-4fc3-968b-79da4356836d"), 0));
				});

		// Check the remaining pages
		assertThat(project.getPages()).hasSize(1);
		assertThat(applicationEvents.stream(PageAddUpdate.class)).isEmpty();
	}

	@Test
	void testDeletePageLastPage() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final UUID pageId = UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea");
		final Page page = project.getPageById(pageId).orElseThrow();

		handler.handleRequest(new PageDeleteRequest(pageId));

		assertThat(project.getPageById(pageId)).isEmpty();
		assertThat(page.getPads())
				.isNotEmpty()
				.allSatisfy(pad -> assertThat(projectController.getPadController(pad.getId())).isNull());
		assertThat(applicationEvents.stream(PageDeleteUpdate.class))
				.hasSize(1)
				.last()
				.satisfies(event -> {
					assertThat(event.getPageId()).isEqualTo(pageId);
					assertThat(event.getPositions()).isEmpty();
				});

		// Check the newly created page
		assertThat(project.getPages()).hasSize(1);
		assertThat(applicationEvents.stream(PageAddUpdate.class))
				.hasSize(1)
				.last()
				.satisfies(event -> {
					assertThat(event.getPage().id()).isNotNull().isNotEqualTo(pageId);
					assertThat(event.getPage().pads()).hasSize(project.getMetadata().getNumberOfPadsPerPage());
				});
	}

	@Test
	void testDeletePagePageNotExists() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final UUID pageId = UUID.fromString("1e76b8b3-ad58-4533-aa57-e2b66360e9ea");
		assertThatThrownBy(() -> handler.handleRequest(new PageDeleteRequest(pageId)))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(PageNotExistsError.class);
	}

	@Test
	void testAddPageProjectNotLoaded()
	{
		final UUID pageId = UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea");
		assertThatThrownBy(() -> handler.handleRequest(new PageDeleteRequest(pageId)))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(ProjectNotLoadedError.class);

		verify(projectService, never()).deletePage(any(), any());
	}

	@Test
	void testUndoOperationForDeletingSomePage() throws Exception
	{
		final UUID padId = UUID.fromString("535e5130-2e46-4865-bb24-e2a55ac793f7");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());

		final PageDeleteRequest request = new PageDeleteRequest(UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677"));

		testInverseOperation(project, handler, request);

		// Check if the pad is loaded again
		assertThat(projectController.getPadController(padId)).isNotNull()
				.satisfies(controller -> assertThat(controller.getStatus()).isEqualTo(PadControllerStatus.READY));
	}

	@Test
	void testUndoOperationForDeletingLastPage() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());

		final PageDeleteRequest request = new PageDeleteRequest(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"));

		testInverseOperation(project, handler, request);

		// Check if the pad is loaded again
		assertThat(projectController.getPadController(padId)).isNotNull()
				.satisfies(controller -> assertThat(controller.getStatus()).isEqualTo(PadControllerStatus.READY));
	}
}