package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.api.project.request.ProjectAddRequest;
import de.tobias.playwall.common.api.project.request.ProjectAddResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectNameAlreadyExistsException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.common.storage.PathProvider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ProjectAddHandlerTest extends AbstractRequestHandlerTest
{
	@TempDir
	private Path tempDir;

	@MockitoBean
	private PathProvider pathProvider;

	@MockitoSpyBean
	private AllProjectsInfoRepository allProjectsInfoRepository;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectAddHandler handler;

	@Captor
	private ArgumentCaptor<Project> projectCaptor;

	@SneakyThrows
	@BeforeEach
	void init()
	{
		when(pathProvider.getPathForConfig(any())).thenReturn(tempDir.resolve("projects.json"));
		when(pathProvider.getPathForProject(any())).thenReturn(tempDir.resolve("project.json"));
		Files.deleteIfExists(tempDir.resolve("projects.json"));
		allProjectsInfoRepository.loadAllProjectsInfo();
		allProjectsInfoRepository.getAllProjects().clear();
	}

	@Test
	void testAddProjectSuccessful() throws Exception
	{
		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectAddRequest("Name1", 5, 4));

		verify(allProjectsInfoRepository).addProject("Name1", 5, 4);
		verify(projectRepository).saveProject(projectCaptor.capture());

		assertThat(response).isNotEmpty();

		final ProjectMetadataDto createdProject = ((ProjectAddResponse) response.get()).getProject();
		assertThat(createdProject.id()).isNotNull();
		assertThat(createdProject.name()).isEqualTo("Name1");
		assertThat(createdProject.numberOfHorizontalPads()).isEqualTo(5);
		assertThat(createdProject.numberOfVerticalPads()).isEqualTo(4);

		final Project project = projectCaptor.getValue();
		assertThat(project.getMetadata().getId()).isEqualTo(createdProject.id());
		assertThat(project.getPages()).hasSize(1);
		assertThat(project.getPages().getFirst().getPads()).hasSize(5 * 4);
	}

	@Test
	void testAddProjectNameAlreadyExists() throws Exception
	{
		final ProjectMetadata projectMetadata = allProjectsInfoRepository.addProject("Name1", 3, 3);
		when(projectRepository.loadProjectMetadata(projectMetadata.getId())).thenReturn(projectMetadata);

		assertThat(allProjectsInfoRepository.getAllProjects()).hasSize(1);

		final ProjectAddRequest request = new ProjectAddRequest("Name1", 5, 4);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNameAlreadyExistsException.class);

		assertThat(allProjectsInfoRepository.getAllProjects()).hasSize(1);
	}
}