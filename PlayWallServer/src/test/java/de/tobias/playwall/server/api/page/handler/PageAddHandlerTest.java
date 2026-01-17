package de.tobias.playwall.server.api.page.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.page.request.PageAddRequest;
import de.tobias.playwall.common.api.page.request.PageAddResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
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

		final Optional<ResponseMessage> response = handler.handleRequest(new PageAddRequest("New Page"));
		assertThat(response).isNotEmpty();

		final PageDto createdPage = ((PageAddResponse) response.get()).getPage();

		assertThat(createdPage.id()).isNotNull();
		assertThat(createdPage.name()).isEqualTo("New Page");
		assertThat(createdPage.position()).isEqualTo(1);
		assertThat(createdPage.pads()).hasSize(6 * 4);
	}

	@Test
	void testAddPageProjectNotLoaded() throws Exception
	{
		assertThatThrownBy(() -> handler.handleRequest(new PageAddRequest("New Page")))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(ProjectNotLoadedError.class);

		verify(projectService, never()).addProject(any(), anyInt(), anyInt());
	}
}