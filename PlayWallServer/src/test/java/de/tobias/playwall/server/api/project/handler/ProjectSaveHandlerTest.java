package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectSaveRequest;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectSaveHandlerTest extends AbstractRequestHandlerTest
{
	@Autowired
	private JsonMapper objectMapper;

	@MockitoBean
	private AllProjectsInfoRepository allProjectsInfoRepository;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private ProjectSaveHandler handler;

	@MockitoBean
	private PathProvider pathProvider;

	@BeforeEach
	void init()
	{
		projectController.unloadProject();
	}

	@Test
	void testProjectSaveRequestSuccessful() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		handler.handleRequest(new ProjectSaveRequest());

		verify(projectRepository).saveProject(project);
		verify(allProjectsInfoRepository).saveAllProjectsInfo();
	}

	@Test
	void testProjectSaveRequestNoLoaded() throws Exception
	{
		final ProjectSaveRequest request = new ProjectSaveRequest();
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotLoadedException.class);

		verify(projectRepository, never()).saveProject(any());
	}
}
