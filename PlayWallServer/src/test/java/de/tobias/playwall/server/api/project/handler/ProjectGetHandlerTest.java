package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.api.project.request.ProjectGetRequest;
import de.tobias.playwall.common.api.project.request.ProjectGetResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.common.model.project.Project;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectGetHandlerTest
{
	@Autowired
	private JsonMapper objectMapper;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectGetHandler handler;

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
}
