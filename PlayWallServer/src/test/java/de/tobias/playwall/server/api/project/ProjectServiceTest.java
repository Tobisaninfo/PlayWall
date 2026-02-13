package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.page.PageNameAlreadyExistsException;
import de.tobias.playwall.server.api.page.PageNotExistsException;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.common.storage.PathProvider;
import org.assertj.core.groups.Tuple;
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
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
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
	private JsonMapper objectMapper;

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
		final Page page = projectService.addPage(project);

		assertThat(page.getId()).isNotNull();
		assertThat(page)
				.extracting(Page::getPosition, Page::getName)
				.containsExactly(0, "Seite 1");

		assertThat(project.getPages().getLast().getPads()).hasSize(6 * 4);
	}

	@Test
	void testAddPageNameAlreadyExists()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		final Page page = projectService.addPage(project);

		assertThat(page.getId()).isNotNull();
		assertThat(page)
				.extracting(Page::getPosition, Page::getName)
				.containsExactly(2, "Seite 4");

		assertThat(project.getPages().getLast().getPads()).hasSize(6 * 4);
	}

	@Test
	void testRenamePage() throws PageNotExistsException, PageNameAlreadyExistsException
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final UUID pageId = UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea");
		projectService.renamePage(project, pageId, "Updated Page Name");

		assertThat(project.getPageById(pageId).orElseThrow())
				.extracting(Page::getPosition, Page::getName)
				.containsExactly(0, "Updated Page Name");
	}

	@Test
	void testRenamePageDuplicateName()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		final UUID pageId = UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677");
		assertThatThrownBy(() -> projectService.renamePage(project, pageId, "Seite 3"))
				.isInstanceOf(PageNameAlreadyExistsException.class);

		assertThat(project.getPageById(pageId).orElseThrow())
				.extracting(Page::getPosition, Page::getName)
				.containsExactly(0, "Seite 1");
	}

	@Test
	void test_renamePage_unknownPage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		assertThatThrownBy(() -> projectService.renamePage(project, UUID.randomUUID(), "Updated Page Name")).isInstanceOf(PageNotExistsException.class);
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
		final Page newPage = projectService.duplicatePage(project, page.getId());

		assertThat(newPage)
				.extracting(Page::getPosition, Page::getName, i -> i.getPads().size())
				.containsExactly(1, "Page 1 - 1", page.getPads().size());
		assertThat(newPage.getId()).isNotEqualTo(page.getId());
		assertThat(newPage.getPads().getFirst().getId()).isNotEqualTo(page.getPads().getFirst().getId());
	}

	@Test
	void testDuplicatePage_nameConflict() throws PageNotExistsException
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final UUID page2Id = UUID.randomUUID();
		project.getPages().add(Page.builder().id(page2Id).name("Page 1 - 1").position(1).build());
		final Page page = project.getPageById(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea")).orElseThrow();
		final Page newPage = projectService.duplicatePage(project, page.getId());

		assertThat(newPage)
				.extracting(Page::getPosition, Page::getName, i -> i.getPads().size())
				.containsExactly(1, "Page 1 - 2", page.getPads().size());
		assertThat(newPage.getId()).isNotEqualTo(page.getId());
		assertThat(newPage.getPads().getFirst().getId()).isNotEqualTo(page.getPads().getFirst().getId());

		assertThat(project.getPages()).hasSize(3)
				.extracting(Page::getId, Page::getPosition)
				.containsExactlyInAnyOrder(
						Tuple.tuple(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 0),
						Tuple.tuple(newPage.getId(), 1),
						Tuple.tuple(page2Id, 2)
				);
	}

	@Test
	void test_duplicatePage_unknownPage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		assertThatThrownBy(() -> projectService.duplicatePage(project, UUID.randomUUID())).isInstanceOf(PageNotExistsException.class);
	}

	@Test
	void test_replacePage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");

		projectService.replacePage(project,
				Page.builder()
						.id(UUID.fromString("4480bbf8-ef96-4592-97e3-cc9a2b6fa786"))
						.name("Seite X")
						.build(),
				0);

		assertThat(project.getPages()).containsExactly(Page.builder()
				.id(UUID.fromString("4480bbf8-ef96-4592-97e3-cc9a2b6fa786"))
				.name("Seite X")
				.position(0)
				.build());
	}

	@Test
	void test_insertPage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");

		final Page addedPage = projectService.addPage(project);

		projectService.insertPage(project,
				Page.builder()
						.id(UUID.fromString("4480bbf8-ef96-4592-97e3-cc9a2b6fa786"))
						.name("Seite X").build(),
				1);


		assertThat(project.getPages()).hasSize(3);
		assertThat(project.getPages().get(0)).extracting(Page::getId, Page::getPosition).containsExactly(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 0);
		assertThat(project.getPages().get(1)).extracting(Page::getId, Page::getPosition).containsExactly(UUID.fromString("4480bbf8-ef96-4592-97e3-cc9a2b6fa786"), 1);
		assertThat(project.getPages().get(2)).extracting(Page::getId, Page::getPosition).containsExactly(addedPage.getId(), 2);
	}
}
