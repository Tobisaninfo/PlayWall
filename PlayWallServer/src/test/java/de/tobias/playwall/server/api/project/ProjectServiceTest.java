package de.tobias.playwall.server.api.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.page.PageNotExistsException;
import de.tobias.playwall.server.common.model.project.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.common.storage.PathProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class ProjectServiceTest
{
	@TempDir
	private Path tempDir;
	@MockitoBean
	private PathProvider pathProvider;

	@MockitoSpyBean
	private ProjectRepository projectRepository;
	@MockitoSpyBean
	private ProjectMetadataRepository projectMetadataRepository;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ObjectMapper objectMapper;

	@Captor
	private ArgumentCaptor<Project> projectCaptor;

	@BeforeEach
	void beforeEach() throws IOException
	{
		when(pathProvider.getPathForConfig(any())).thenReturn(tempDir.resolve("projects.json"));
		projectMetadataRepository.clearProjects();
	}

	@Test
	void testProjectAdd() throws ProjectNameAlreadyExistsException, IOException
	{
		final ProjectMetadata createdProject = projectService.addProject("New Project", 5, 4);

		verify(projectMetadataRepository).addProject("New Project", 5, 4);
		verify(projectRepository).saveProject(projectCaptor.capture());

		assertThat(createdProject.getId()).isNotNull();
		assertThat(createdProject.getName()).isEqualTo("New Project");
		assertThat(createdProject.getNumberOfHorizontalPads()).isEqualTo(5);
		assertThat(createdProject.getNumberOfVerticalPads()).isEqualTo(4);

		final Project project = projectCaptor.getValue();
		assertThat(project.getMetadata().getId()).isEqualTo(createdProject.getId());
		assertThat(project.getPages()).hasSize(1);
		assertThat(project.getPages().getFirst().getName()).isEqualTo("Seite 1");
		assertThat(project.getPages().getFirst().getPads()).hasSize(5 * 4);
	}

	@Test
	void testAddPage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_2.json");
		final Page page = projectService.addPage(project, "New Page");

		assertThat(page)
				.extracting(Page::getPosition, Page::getName)
				.containsExactly(0, "New Page");

		assertThat(project.getPages().getFirst().getPads()).hasSize(6 * 4);
	}

	@Test
	void testRenamePage() throws PageNotExistsException
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final Page newPage = projectService.renamePage(project, UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), "Updated Page Name");

		assertThat(newPage)
				.extracting(Page::getPosition, Page::getName)
				.containsExactly(0, "Updated Page Name");
	}

	@Test
	void test_renamePage_unknownPage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		assertThatThrownBy(() -> {
			projectService.renamePage(project, UUID.randomUUID(), "Updated Page Name");
		}).isInstanceOf(PageNotExistsException.class);
	}

	@Test
	void testDeletePage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");

		final boolean isSuccess = projectService.deletePage(project, UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"));

		assertThat(isSuccess).isTrue();
		assertThat(project.getPages()).isEmpty();
	}

	@Test
	void testDeletePageUnknownPage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");

		final boolean isSuccess = projectService.deletePage(project, UUID.randomUUID());
		assertThat(isSuccess).isFalse();
	}

	@Test
	void testDuplicatePage() throws PageNotExistsException
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final Page page = project.getPageById(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea")).orElseThrow();
		final Page newPage = projectService.duplicatePage(project, page.getId(), "Duplicated Page");

		assertThat(newPage)
				.extracting(Page::getPosition, Page::getName, i -> i.getPads().size())
				.containsExactly(1, "Duplicated Page", page.getPads().size());
		assertThat(newPage.getId()).isNotEqualTo(page.getId());
		assertThat(newPage.getPads().getFirst().getId()).isNotEqualTo(page.getPads().getFirst().getId());
	}

	@Test
	void test_duplicatePage_unknownPage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		assertThatThrownBy(() -> {
			projectService.duplicatePage(project, UUID.randomUUID(), "Duplicated Page");
		}).isInstanceOf(PageNotExistsException.class);
	}
}
