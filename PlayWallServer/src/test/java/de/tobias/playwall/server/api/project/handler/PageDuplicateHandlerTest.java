package de.tobias.playwall.server.api.project.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.project.PageAddResponse;
import de.tobias.playwall.common.api.project.PageDuplicateRequest;
import de.tobias.playwall.common.api.project.PageNotExistsError;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.api.project.model.PageDto;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PageDuplicateHandlerTest
{
	@Autowired
	private ProjectController projectController;

	@MockitoSpyBean
	private ProjectService projectService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PageDuplicateHandler handler;

	@BeforeEach
	void init()
	{
		projectController.unloadProject();
	}

	@Test
	void testDuplicatePageSuccessful() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final UUID originalPage = UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea");
		final Optional<ResponseMessage> response = handler.handleRequest(new PageDuplicateRequest(originalPage, "Duplicated Page"));
		assertThat(response).isNotEmpty();

		final PageDto createdPage = ((PageAddResponse) response.get()).getPage();

		assertThat(createdPage.id()).isNotEqualTo(originalPage).isNotNull();
		assertThat(createdPage.name()).isEqualTo("Duplicated Page");
		assertThat(createdPage.position()).isEqualTo(1);
		assertThat(createdPage.pads()).hasSize(project.getPageById(originalPage).orElseThrow().getPads().size());
	}

	@Test
	void testDuplicatePagePageNotExists() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final UUID originalPage = UUID.fromString("1e76b8b3-2da8-4533-aa57-e2b66360e9ea");
		assertThatThrownBy(() -> handler.handleRequest(new PageDuplicateRequest(originalPage, "Duplicated Page")))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(PageNotExistsError.class);
	}

	@Test
	void testDuplicatePageProjectNotLoaded() throws Exception
	{
		final UUID originalPage = UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea");
		assertThatThrownBy(() -> handler.handleRequest(new PageDuplicateRequest(originalPage, "Duplicated Page")))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(ProjectNotLoadedError.class);

		verify(projectService, never()).duplicatePage(any(), any(), any());
	}
}