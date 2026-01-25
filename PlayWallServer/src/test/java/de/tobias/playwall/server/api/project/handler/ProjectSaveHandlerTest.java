package de.tobias.playwall.server.api.project.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.api.project.request.ProjectSaveRequest;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.PlayWallServerException;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectSaveHandlerTest
{
	@TempDir
	private Path tempDir;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private ProjectSaveHandler handler;

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
	void testProjectSaveRequestSuccessful() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		handler.handleRequest(new ProjectSaveRequest());

		verify(projectRepository).saveProject(project);
	}

	@Test
	void testProjectSaveRequestNoLoaded() throws Exception
	{
		assertThatThrownBy(() -> handler.handleRequest(new ProjectSaveRequest()))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(ProjectNotLoadedError.class);

		verify(projectRepository, never()).saveProject(any());
	}
}
