package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.common.storage.PathProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class ProjectMetadataRepositoryTest
{
	@TempDir
	private Path tempDir;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@MockitoBean
	private PathProvider pathProvider;

	@BeforeEach
	void beforeEach() throws IOException
	{
		when(pathProvider.getPathForConfig(any())).thenReturn(tempDir.resolve("projects.json"));
		projectMetadataRepository.clearProjects();
	}

	@Autowired
	private ProjectMetadataRepository projectMetadataRepository;

	@Test
	void test_noProjects()
	{
		assertThat(projectMetadataRepository.getAllProjectMetadata()).isEmpty();
	}

	@Test
	void test_addProject() throws IOException, ProjectNameAlreadyExistsException
	{
		final ProjectMetadata projectMetadata = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);

		assertThat(projectMetadata)
				.extracting(ProjectMetadata::getName, ProjectMetadata::getNumberOfHorizontalPads, ProjectMetadata::getNumberOfVerticalPads, ProjectMetadata::getVolume)
				.containsExactly("New ProjectMetadata", 6, 5, 1.0);

		final ProjectMetadata expected = ProjectMetadata.builder()
				.id(projectMetadata.getId())
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.volume(1.0)
				.build();

		assertThat(projectMetadataRepository.getAllProjectMetadata()).containsExactly(expected);
	}

	@Test
	void test_addProject_nameAlreadyExists() throws IOException, ProjectNameAlreadyExistsException
	{
		projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);
		assertThatThrownBy(() -> projectMetadataRepository.addProject("New ProjectMetadata", 4, 3)).isInstanceOf(ProjectNameAlreadyExistsException.class);
	}

	@Test
	void test_getProjectMetadataById() throws IOException, ProjectNotExistsException, ProjectNameAlreadyExistsException
	{
		final ProjectMetadata projectMetadata = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);

		final ProjectMetadata expected = ProjectMetadata.builder()
				.id(projectMetadata.getId())
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.volume(1.0)
				.build();

		assertThat(projectMetadataRepository.getProjectMetadataById(projectMetadata.getId())).isEqualTo(expected);
	}

	@Test
	void test_getProjectMetadataById_noMatch()
	{
		assertThatThrownBy(() -> projectMetadataRepository.getProjectMetadataById(UUID.randomUUID())).isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void test_deleteProject() throws IOException, ProjectNameAlreadyExistsException
	{
		final ProjectMetadata projectMetadata = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);
		assertThat(projectMetadataRepository.deleteProject(projectMetadata.getId())).isTrue();
		assertThat(projectMetadataRepository.getAllProjectMetadata()).isEmpty();
	}
}
