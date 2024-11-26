package de.tobias.playwall.server.api.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProjectRepositoryTest
{
//	@TestConfiguration
//	public static class TestConfig
//	{
//		@Bean
//		@Primary
//		public PathProvider pathProvider() throws IOException
//		{
//			final Path tempFolder = Files.createTempDirectory("ProjectRepositoryTest").toAbsolutePath();
//			final PathProvider mock = mock(PathProvider.class);
//			when(mock.getPathForConfig(any())).thenReturn(tempFolder.resolve("projects.json"));
//			return mock;
//		}
//	}

	@BeforeEach
	void beforeEach() throws IOException
	{
		projectRepository.clearProjects();
	}

	@Autowired
	private ProjectRepository projectRepository;

	@Test
	void test_noProjects()
	{
		assertThat(projectRepository.getAllProjectMetadata()).isEmpty();
	}

	@Test
	void test_addProject() throws IOException
	{
		final Optional<Project> projectOptional = projectRepository.addProject("New Project", 6, 5);

		assertThat(projectOptional).isPresent().get()
				.extracting(Project::getName, Project::getNumberOfHorizontalPads, Project::getNumberOfVerticalPads, Project::getPages)
				.containsExactly("New Project", 6, 5, List.of());

		final Project expected = Project.builder()
				.id(projectOptional.get().getId())
				.name("New Project")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.pages(List.of())
				.build();

		assertThat(projectRepository.getAllProjectMetadata()).containsExactly(expected);
	}

	@Test
	void test_addProject_nameAlreadyExist() throws IOException
	{
		projectRepository.addProject("New Project", 6, 5);

		final Optional<Project> projectOptional = projectRepository.addProject("New Project", 4, 3);

		assertThat(projectOptional).isEmpty();
		assertThat(projectRepository.getAllProjectMetadata()).hasSize(1);
	}

	@Test
	void test_getProjectById() throws IOException
	{
		final Optional<Project> projectOptional = projectRepository.addProject("New Project", 6, 5);
		final UUID projectId = projectOptional.get().getId();

		final Project expected = Project.builder()
				.id(projectId)
				.name("New Project")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.pages(List.of())
				.build();

		assertThat(projectRepository.getProjectById(projectId)).isPresent().get().isEqualTo(expected);
	}

	@Test
	void test_getProjectById_noMatch()
	{
		assertThat(projectRepository.getProjectById(UUID.randomUUID())).isEmpty();
	}

	@Test
	void test_deleteProject() throws IOException
	{
		final Optional<Project> projectOptional = projectRepository.addProject("New Project", 6, 5);
		final UUID projectId = projectOptional.get().getId();

		assertThat(projectRepository.deleteProject(projectId)).isTrue();
		assertThat(projectRepository.getAllProjectMetadata()).isEmpty();
	}

	@Test
	void test_addPage() throws IOException
	{
		final Optional<Project> projectOptional = projectRepository.addProject("New Project", 6, 5);

		final Optional<Page> pageOptional = projectRepository.addPage(projectOptional.get().getId(), "New Page");

		assertThat(pageOptional).isPresent().get()
				.extracting(Page::getPosition, Page::getName, Page::getPads)
				.containsExactly(0, "New Page", List.of());

		final Project expected = Project.builder()
				.id(projectOptional.get().getId())
				.name("New Project")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.pages(List.of(Page.builder()
						.id(pageOptional.get().getId())
						.position(0)
						.name("New Page")
						.pads(List.of())
						.build()))
				.build();

		assertThat(projectRepository.getAllProjectMetadata()).containsExactly(expected);
	}
}
