package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectGetSaveStatusRequest;
import de.tobias.playwall.common.api.project.request.ProjectGetSaveStatusResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
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
class ProjectGetSaveStatusHandlerTest extends AbstractRequestHandlerTest
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
	private ProjectGetSaveStatusHandler handler;

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
	void testProjectGetSaveStatusIsSaved() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final UUID projectId = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		when(projectRepository.loadProject(projectId)).thenReturn(project);

		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectGetSaveStatusRequest());

		assertThat(response).isNotEmpty();
		assertThat(((ProjectGetSaveStatusResponse) response.get()).isSaved()).isTrue();
	}

	@Test
	void testProjectGetSaveStatusHasChanges() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final Project projectCopy = project.copy(false);
		projectCopy.getMetadata().setVolume(0.0);
		projectCopy.getPages().clear();

		final UUID projectId = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		when(projectRepository.loadProject(projectId)).thenReturn(projectCopy);

		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectGetSaveStatusRequest());

		assertThat(response).isNotEmpty();
		assertThat(((ProjectGetSaveStatusResponse) response.get()).isSaved()).isFalse();
	}


	@Test
	void testProjectSaveRequestNoLoaded()
	{
		final ProjectGetSaveStatusRequest request = new ProjectGetSaveStatusRequest();
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotLoadedException.class);
	}
}
