package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.api.project.model.ProjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProjectMetadataRepositoryTest
{
//	@TestConfiguration
//	public static class TestConfig
//	{
//		@Bean
//		@Primary
//		public PathProvider pathProvider() throws IOException
//		{
//			final Path tempFolder = Files.createTempDirectory("ProjectMetadataRepositoryTest").toAbsolutePath();
//			final PathProvider mock = mock(PathProvider.class);
//			when(mock.getPathForConfig(any())).thenReturn(tempFolder.resolve("projects.json"));
//			return mock;
//		}
//	}

	@BeforeEach
	void beforeEach() throws IOException
	{
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
				.extracting(ProjectMetadata::getName, ProjectMetadata::getNumberOfHorizontalPads, ProjectMetadata::getNumberOfVerticalPads)
				.containsExactly("New ProjectMetadata", 6, 5);

		final ProjectMetadata expected = ProjectMetadata.builder()
				.id(projectMetadata.getId())
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
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
