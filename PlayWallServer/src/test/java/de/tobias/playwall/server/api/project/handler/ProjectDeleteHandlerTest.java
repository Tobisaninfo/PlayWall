package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectDeleteRequest;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectDeleteHandlerTest extends AbstractRequestHandlerTest
{
	@MockitoBean
	private AllProjectsInfoRepository allProjectsInfoRepository;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectDeleteHandler handler;

	@Test
	void testDeleteProjectSuccessful() throws Exception
	{
		final UUID id = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		when(allProjectsInfoRepository.deleteProject(id)).thenReturn(true);
		when(projectRepository.deleteProject(id)).thenReturn(true);

		handler.handleRequest(new ProjectDeleteRequest(id));

		verify(allProjectsInfoRepository).deleteProject(id);
		verify(projectRepository).deleteProject(id);
	}

	@Test
	void testDeleteProjectNotFoundMetadata() throws Exception
	{
		final UUID id = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		when(allProjectsInfoRepository.deleteProject(id)).thenReturn(false);
		when(projectRepository.deleteProject(id)).thenReturn(true);

		final ProjectDeleteRequest request = new ProjectDeleteRequest(id);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotExistsException.class);

		verify(allProjectsInfoRepository).deleteProject(id);
		verify(projectRepository, never()).deleteProject(id);
	}

	@Test
	void testDeleteProjectNotFoundProjectFile() throws Exception
	{
		final UUID id = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		when(allProjectsInfoRepository.deleteProject(id)).thenReturn(true);
		when(projectRepository.deleteProject(id)).thenReturn(false);

		final ProjectDeleteRequest request = new ProjectDeleteRequest(id);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotExistsException.class);

		verify(allProjectsInfoRepository).deleteProject(id);
		verify(projectRepository).deleteProject(id);
	}
}