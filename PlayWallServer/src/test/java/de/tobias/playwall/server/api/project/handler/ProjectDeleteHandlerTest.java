package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.api.project.request.ProjectDeleteRequest;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectMetadataRepository;
import de.tobias.playwall.server.api.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectDeleteHandlerTest
{
	@MockitoBean
	private ProjectMetadataRepository projectMetadataRepository;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectDeleteHandler handler;

	@Test
	void testDeleteProjectSuccessful() throws Exception
	{
		final UUID id = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		when(projectMetadataRepository.deleteProject(id)).thenReturn(true);
		when(projectRepository.deleteProject(id)).thenReturn(true);

		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectDeleteRequest(id));

		verify(projectMetadataRepository).deleteProject(id);
		verify(projectRepository).deleteProject(id);

		assertThat(response).isEmpty();
	}

	@Test
	void testDeleteProjectNotFoundMetadata() throws Exception
	{
		final UUID id = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		when(projectMetadataRepository.deleteProject(id)).thenReturn(false);
		when(projectRepository.deleteProject(id)).thenReturn(true);

		assertThatThrownBy(() -> handler.handleRequest(new ProjectDeleteRequest(id)))
				.isInstanceOf(PlayWallServerException.class)
				.hasMessage("Es existiert kein Projekt mit der ID \"fc427184-2e55-4734-8148-5fb657963616\".");

		verify(projectMetadataRepository).deleteProject(id);
		verify(projectRepository, never()).deleteProject(id);
	}

	@Test
	void testDeleteProjectNotFoundProjectFile() throws Exception
	{
		final UUID id = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		when(projectMetadataRepository.deleteProject(id)).thenReturn(true);
		when(projectRepository.deleteProject(id)).thenReturn(false);

		assertThatThrownBy(() -> handler.handleRequest(new ProjectDeleteRequest(id)))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(ProjectNotExistsError.class);

		verify(projectMetadataRepository).deleteProject(id);
		verify(projectRepository).deleteProject(id);
	}
}