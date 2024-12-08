package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.api.project.model.Page;
import de.tobias.playwall.server.api.project.model.ProjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
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
	void test_addProject() throws IOException
	{
		final Optional<ProjectMetadata> projectOptional = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);

		assertThat(projectOptional).isPresent().get()
				.extracting(ProjectMetadata::getName, ProjectMetadata::getNumberOfHorizontalPads, ProjectMetadata::getNumberOfVerticalPads, ProjectMetadata::getPages)
				.containsExactly("New ProjectMetadata", 6, 5, List.of());

		final ProjectMetadata expected = ProjectMetadata.builder()
				.id(projectOptional.get().getId())
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.pages(List.of())
				.build();

		assertThat(projectMetadataRepository.getAllProjectMetadata()).containsExactly(expected);
	}

	@Test
	void test_addProject_nameAlreadyExist() throws IOException
	{
		projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);

		final Optional<ProjectMetadata> projectOptional = projectMetadataRepository.addProject("New ProjectMetadata", 4, 3);

		assertThat(projectOptional).isEmpty();
		assertThat(projectMetadataRepository.getAllProjectMetadata()).hasSize(1);
	}

	@Test
	void test_getProjectById() throws IOException, ProjectNotExistsException
	{
		final Optional<ProjectMetadata> projectOptional = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);
		final UUID projectId = projectOptional.orElseThrow().getId();

		final ProjectMetadata expected = ProjectMetadata.builder()
				.id(projectId)
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.pages(List.of())
				.build();

		assertThat(projectMetadataRepository.getProjectById(projectId)).isEqualTo(expected);
	}

	@Test
	void test_getProjectById_noMatch()
	{
		assertThatThrownBy(() -> projectMetadataRepository.getProjectById(UUID.randomUUID())).isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void test_deleteProject() throws IOException
	{
		final Optional<ProjectMetadata> projectOptional = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);
		final UUID projectId = projectOptional.orElseThrow().getId();

		assertThat(projectMetadataRepository.deleteProject(projectId)).isTrue();
		assertThat(projectMetadataRepository.getAllProjectMetadata()).isEmpty();
	}

	@Test
	void test_addPage() throws IOException, ProjectNotExistsException
	{
		final Optional<ProjectMetadata> projectOptional = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);

		final Page page = projectMetadataRepository.addPage(projectOptional.orElseThrow().getId(), "New Page");

		assertThat(page)
				.extracting(Page::getPosition, Page::getName, Page::getPads)
				.containsExactly(0, "New Page", List.of());

		final ProjectMetadata expected = ProjectMetadata.builder()
				.id(projectOptional.get().getId())
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.pages(List.of(Page.builder()
						.id(page.getId())
						.position(0)
						.name("New Page")
						.pads(List.of())
						.build()))
				.build();

		assertThat(projectMetadataRepository.getAllProjectMetadata()).containsExactly(expected);
	}

	@Test
	void test_addPage_unknownProject()
	{
		assertThatThrownBy(() -> {
			projectMetadataRepository.addPage(UUID.randomUUID(), "New Page");
		}).isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void test_renamePage() throws IOException, ProjectNotExistsException, PageNotExistsException
	{
		final Optional<ProjectMetadata> projectOptional = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);
		final Page page = projectMetadataRepository.addPage(projectOptional.orElseThrow().getId(), "New Page");

		final Page newPage = projectMetadataRepository.renamePage(projectOptional.get().getId(), page.getId(), "Updated Page Name");

		assertThat(newPage)
				.extracting(Page::getPosition, Page::getName, Page::getPads)
				.containsExactly(0, "Updated Page Name", List.of());

		final ProjectMetadata expected = ProjectMetadata.builder()
				.id(projectOptional.get().getId())
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.pages(List.of(Page.builder()
						.id(page.getId())
						.position(0)
						.name("Updated Page Name")
						.pads(List.of())
						.build()))
				.build();

		assertThat(projectMetadataRepository.getAllProjectMetadata()).containsExactly(expected);
	}

	@Test
	void test_renamePage_unknownProject()
	{
		assertThatThrownBy(() -> {
			projectMetadataRepository.renamePage(UUID.randomUUID(), UUID.randomUUID(), "Updated Page Name");
		}).isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void test_renamePage_unknownPage() throws IOException
	{
		final Optional<ProjectMetadata> projectOptional = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);

		assertThatThrownBy(() -> {
			projectMetadataRepository.renamePage(projectOptional.orElseThrow().getId(), UUID.randomUUID(), "Updated Page Name");
		}).isInstanceOf(PageNotExistsException.class);
	}

	@Test
	void test_deletePage() throws IOException, ProjectNotExistsException
	{
		final Optional<ProjectMetadata> projectOptional = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);
		final Page page = projectMetadataRepository.addPage(projectOptional.orElseThrow().getId(), "New Page");

		final boolean isSuccess = projectMetadataRepository.deletePage(projectOptional.get().getId(), page.getId());

		assertThat(isSuccess).isTrue();

		final ProjectMetadata expected = ProjectMetadata.builder()
				.id(projectOptional.get().getId())
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.pages(List.of())
				.build();

		assertThat(projectMetadataRepository.getAllProjectMetadata()).containsExactly(expected);
	}

	@Test
	void test_deletePage_unknownProject()
	{
		assertThatThrownBy(() -> {
			projectMetadataRepository.deletePage(UUID.randomUUID(), UUID.randomUUID());
		}).isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void test_deletePage_unknownPage() throws IOException, ProjectNotExistsException
	{
		final Optional<ProjectMetadata> projectOptional = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);

		final boolean isSuccess = projectMetadataRepository.deletePage(projectOptional.orElseThrow().getId(), UUID.randomUUID());

		assertThat(isSuccess).isFalse();
	}

	@Test
	void test_duplicatePage() throws IOException, ProjectNotExistsException, PageNotExistsException
	{
		final Optional<ProjectMetadata> projectOptional = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);

		final Page page = projectMetadataRepository.addPage(projectOptional.orElseThrow().getId(), "New Page");
		final Page newPage = projectMetadataRepository.duplicatePage(projectOptional.get().getId(), page.getId(), "Duplicated Page");

		assertThat(newPage)
				.extracting(Page::getPosition, Page::getName, Page::getPads)
				.containsExactly(1, "Duplicated Page", List.of());

		final ProjectMetadata expected = ProjectMetadata.builder()
				.id(projectOptional.get().getId())
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.pages(List.of(Page.builder()
								.id(page.getId())
								.position(0)
								.name("New Page")
								.pads(List.of())
								.build(),
						Page.builder()
								.id(newPage.getId())
								.position(1)
								.name("Duplicated Page")
								.pads(List.of())
								.build()))
				.build();

		assertThat(projectMetadataRepository.getAllProjectMetadata()).containsExactly(expected);
	}

	@Test
	void test_duplicatePage_unknownProject()
	{
		assertThatThrownBy(() -> {
			projectMetadataRepository.duplicatePage(UUID.randomUUID(), UUID.randomUUID(), "Duplicated Page");
		}).isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void test_duplicatePage_unknownPage() throws IOException
	{
		final Optional<ProjectMetadata> projectOptional = projectMetadataRepository.addProject("New ProjectMetadata", 6, 5);

		assertThatThrownBy(() -> {
			projectMetadataRepository.duplicatePage(projectOptional.orElseThrow().getId(), UUID.randomUUID(), "Duplicated Page");
		}).isInstanceOf(PageNotExistsException.class);
	}
}
