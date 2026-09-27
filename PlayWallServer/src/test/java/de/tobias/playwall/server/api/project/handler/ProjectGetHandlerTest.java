package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectGetRequest;
import de.tobias.playwall.common.api.project.request.ProjectGetResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectGetHandlerTest extends AbstractRequestHandlerTest
{
	@TempDir
	private Path tempDir;

	@Autowired
	private JsonMapper objectMapper;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private ProjectGetHandler handler;

	@MockitoBean
	private PathProvider pathProvider;

	@BeforeEach
	void init() throws IOException
	{
		final Path projectsFile = tempDir.resolve("projects.json");

		Files.writeString(projectsFile, "[]");

		when(pathProvider.getPathForConfig(any())).thenReturn(projectsFile);
		projectController.unloadProject();
	}

	@Test
	void testProjectGetRequestSuccessful() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final UUID projectId = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		when(projectRepository.loadProject(projectId)).thenReturn(project);

		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectGetRequest(projectId));

		assertThat(response).isNotEmpty();

		assertThat(((ProjectGetResponse) response.get()).getProject().metadata().id()).isEqualTo(projectId);
	}

	@Test
	void testProjectGetRequestNotFound() throws Exception
	{
		final UUID projectId = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		when(projectRepository.loadProject(projectId)).thenThrow(new ProjectNotExistsException(projectId));

		final ProjectGetRequest request = new ProjectGetRequest(projectId);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void testProjectGetRequestWithoutIdReturnsLoadedProject() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final UUID projectId = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		projectController.loadProject(project).get();
		projectController.setCurrentPageIndex(2);

		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectGetRequest(null));

		assertThat(response).isNotEmpty();
		final ProjectGetResponse projectGetResponse = (ProjectGetResponse) response.get();
		assertThat(projectGetResponse.getProject().metadata().id()).isEqualTo(projectId);
		assertThat(projectGetResponse.getCurrentPageIndex()).isEqualTo(2);
	}

	@Test
	void testProjectGetRequestByIdDoesNotReturnCurrentPageIndex() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final UUID projectId = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		when(projectRepository.loadProject(projectId)).thenReturn(project);
		projectController.loadProject(project).get();
		projectController.setCurrentPageIndex(2);

		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectGetRequest(projectId));

		assertThat(response).isNotEmpty();
		final ProjectGetResponse projectGetResponse = (ProjectGetResponse) response.get();
		assertThat(projectGetResponse.getCurrentPageIndex()).isNull();
		assertThat(projectGetResponse.getPadStatusById()).isNull();
	}

	@Test
	void testProjectGetRequestWithoutIdReturnsPadStatuses() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectGetRequest(null));

		assertThat(response).isNotEmpty();
		assertThat(((ProjectGetResponse) response.get()).getPadStatusById()).isEqualTo(projectController.getAllPadStatusById());
	}

	@Test
	void testProjectGetRequestWithoutIdAndNoProjectLoaded()
	{
		final ProjectGetRequest request = new ProjectGetRequest(null);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotLoadedException.class);
	}
}
