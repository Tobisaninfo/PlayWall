package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.api.project.model.Page;
import de.tobias.playwall.server.api.project.model.Project;
import de.tobias.playwall.server.api.project.model.ProjectMetadata;
import de.tobias.playwall.server.storage.PathProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class ProjectRepositoryTest
{
	@TestConfiguration
	public static class TestConfig
	{
		@Bean
		@Primary
		public PathProvider pathProvider() throws IOException
		{
			final Path tempFolder = Files.createTempDirectory("ProjectMetadataRepositoryTest").toAbsolutePath();
			final PathProvider mock = mock(PathProvider.class);
			when(mock.getPathForConfig(any())).thenAnswer(i -> tempFolder.resolve((String) i.getArguments()[0]));
			return mock;
		}
	}

	private static final UUID PROJECT_ID = UUID.randomUUID();


	@Autowired
	private ProjectRepository projectRepository;

	@BeforeEach
	void beforeEach() throws IOException
	{
		projectRepository.deleteProject(PROJECT_ID);
		projectRepository.saveProject(getProject());
	}

	private ProjectMetadata getProjectMetadata()
	{
		return ProjectMetadata.builder()
				.id(PROJECT_ID)
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.build();
	}

	private Project getProject()
	{
		return Project.builder()
				.metadata(getProjectMetadata())
				.pages(new ArrayList<>())
				.build();
	}

	@Test
	void test_addPage() throws IOException, ProjectNotExistsException
	{
		final Page page = projectRepository.addPage(PROJECT_ID, "New Page");

		assertThat(page)
				.extracting(Page::getPosition, Page::getName, Page::getPads)
				.containsExactly(0, "New Page", List.of());

		final Project expected = Project.builder()
				.metadata(getProjectMetadata())
				.pages(List.of(Page.builder()
						.id(page.getId())
						.position(0)
						.name("New Page")
						.pads(List.of())
						.build()))
				.build();

		assertThat(projectRepository.loadProject(PROJECT_ID)).isEqualTo(expected);
	}

	@Test
	void test_addPage_unknownProject()
	{
		assertThatThrownBy(() -> {
			projectRepository.addPage(UUID.randomUUID(), "New Page");
		}).isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void test_renamePage() throws IOException, ProjectNotExistsException, PageNotExistsException
	{
		final Page page = projectRepository.addPage(PROJECT_ID, "New Page");

		final Page newPage = projectRepository.renamePage(PROJECT_ID, page.getId(), "Updated Page Name");

		assertThat(newPage)
				.extracting(Page::getPosition, Page::getName, Page::getPads)
				.containsExactly(0, "Updated Page Name", List.of());

		final Project expected = Project.builder()
				.metadata(getProjectMetadata())
				.pages(List.of(Page.builder()
						.id(page.getId())
						.position(0)
						.name("Updated Page Name")
						.pads(List.of())
						.build()))
				.build();

		assertThat(projectRepository.loadProject(PROJECT_ID)).isEqualTo(expected);
	}

	@Test
	void test_renamePage_unknownProject()
	{
		assertThatThrownBy(() -> {
			projectRepository.renamePage(UUID.randomUUID(), UUID.randomUUID(), "Updated Page Name");
		}).isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void test_renamePage_unknownPage()
	{
		assertThatThrownBy(() -> {
			projectRepository.renamePage(PROJECT_ID, UUID.randomUUID(), "Updated Page Name");
		}).isInstanceOf(PageNotExistsException.class);
	}

	@Test
	void test_deletePage() throws IOException, ProjectNotExistsException
	{
		final Page page = projectRepository.addPage(PROJECT_ID, "New Page");

		final boolean isSuccess = projectRepository.deletePage(PROJECT_ID, page.getId());

		assertThat(isSuccess).isTrue();

		final Project expected = Project.builder()
				.metadata(getProjectMetadata())
				.pages(List.of())
				.build();

		assertThat(projectRepository.loadProject(PROJECT_ID)).isEqualTo(expected);
	}

	@Test
	void test_deletePage_unknownProject()
	{
		assertThatThrownBy(() -> {
			projectRepository.deletePage(UUID.randomUUID(), UUID.randomUUID());
		}).isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void test_deletePage_unknownPage() throws IOException, ProjectNotExistsException
	{
		final boolean isSuccess = projectRepository.deletePage(PROJECT_ID, UUID.randomUUID());
		assertThat(isSuccess).isFalse();
	}

	@Test
	void test_duplicatePage() throws IOException, ProjectNotExistsException, PageNotExistsException
	{
		final Page page = projectRepository.addPage(PROJECT_ID, "New Page");
		final Page newPage = projectRepository.duplicatePage(PROJECT_ID, page.getId(), "Duplicated Page");

		assertThat(newPage)
				.extracting(Page::getPosition, Page::getName, Page::getPads)
				.containsExactly(1, "Duplicated Page", List.of());

		final Project expected = Project.builder()
				.metadata(getProjectMetadata())
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

		assertThat(projectRepository.loadProject(PROJECT_ID)).isEqualTo(expected);
	}

	@Test
	void test_duplicatePage_unknownProject()
	{
		assertThatThrownBy(() -> {
			projectRepository.duplicatePage(UUID.randomUUID(), UUID.randomUUID(), "Duplicated Page");
		}).isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void test_duplicatePage_unknownPage()
	{
		assertThatThrownBy(() -> {
			projectRepository.duplicatePage(PROJECT_ID, UUID.randomUUID(), "Duplicated Page");
		}).isInstanceOf(PageNotExistsException.class);
	}
}
