package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.page.PageNotExistsException;
import de.tobias.playwall.server.api.settings.SettingsService;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.page.PageSettings;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.common.model.settings.Settings;
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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
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

	@MockitoBean
	private SettingsService settingsService;

	@MockitoSpyBean
	private ProjectRepository projectRepository;

	@MockitoSpyBean
	private AllProjectsInfoRepository allProjectsInfoRepository;

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
		when(pathProvider.getPathForProject(any())).thenReturn(tempDir.resolve("project.json"));
		allProjectsInfoRepository.loadAllProjectsInfo();
		allProjectsInfoRepository.clearProjects();
		when(settingsService.getSettings()).thenReturn(Settings.DEFAULT);
	}

	@Test
	void testProjectAdd() throws ProjectNameAlreadyExistsException, IOException
	{
		final ProjectMetadata createdProject = projectService.addProject("New Project", 5, 4);

		verify(allProjectsInfoRepository).addProject("New Project", 5, 4);
		verify(projectRepository).saveProject(projectCaptor.capture());

		assertThat(createdProject.getId()).isNotNull();
		assertThat(createdProject.getName()).isEqualTo("New Project");
		assertThat(createdProject.getNumberOfHorizontalPads()).isEqualTo(5);
		assertThat(createdProject.getNumberOfVerticalPads()).isEqualTo(4);

		final Project project = projectCaptor.getValue();
		assertThat(project.getMetadata().getId()).isEqualTo(createdProject.getId());
		assertThat(project.getPages()).hasSize(1);
		assertThat(project.getPages().getFirst().getSettings().getName()).isEqualTo("Seite 1");
		assertThat(project.getPages().getFirst().getPads()).hasSize(5 * 4);
	}

	@Test
	void testAddPage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_2.json");
		final Page page = projectService.addPage(project);

		assertThat(page.getId()).isNotNull();
		assertThat(page)
				.extracting(Page::getPosition, e -> e.getSettings().getName())
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
				.extracting(Page::getPosition, e -> e.getSettings().getName())
				.containsExactly(2, "Seite 4");

		assertThat(project.getPages().getLast().getPads()).hasSize(6 * 4);
	}

	@Test
	void testRenamePage() throws PageNotExistsException
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final UUID pageId = UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea");
		projectService.renamePage(project, pageId, "Updated Page Name");

		assertThat(project.getPageById(pageId).orElseThrow())
				.extracting(Page::getPosition, e -> e.getSettings().getName())
				.containsExactly(0, "Updated Page Name");
	}

	@Test
	void test_renamePage_unknownPage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final UUID uuid = UUID.randomUUID();
		assertThatThrownBy(() -> projectService.renamePage(project, uuid, "Updated Page Name")).isInstanceOf(PageNotExistsException.class);
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
				.extracting(Page::getPosition, e -> e.getSettings().getName(), i -> i.getPads().size())
				.containsExactly(1, "Page 1 - 1", page.getPads().size());
		assertThat(newPage.getId()).isNotEqualTo(page.getId());
		assertThat(newPage.getPads().getFirst().getId()).isNotEqualTo(page.getPads().getFirst().getId());
	}

	@Test
	void testDuplicatePage_nameConflict() throws PageNotExistsException
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final UUID page2Id = UUID.randomUUID();
		project.getPages().add(Page.builder().id(page2Id).settings(PageSettings.builder().name("Page 1 - 1").build()).position(1).build());
		final Page page = project.getPageById(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea")).orElseThrow();
		final Page newPage = projectService.duplicatePage(project, page.getId());

		assertThat(newPage)
				.extracting(Page::getPosition, e -> e.getSettings().getName(), i -> i.getPads().size())
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
		final UUID uuid = UUID.randomUUID();
		assertThatThrownBy(() -> projectService.duplicatePage(project, uuid)).isInstanceOf(PageNotExistsException.class);
	}

	@Test
	void test_replacePage()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");

		projectService.replacePage(project,
				Page.builder()
						.id(UUID.fromString("4480bbf8-ef96-4592-97e3-cc9a2b6fa786"))
						.settings(PageSettings.builder()
								.name("Seite X")
								.build())
						.build(),
				0);

		assertThat(project.getPages()).containsExactly(Page.builder()
				.id(UUID.fromString("4480bbf8-ef96-4592-97e3-cc9a2b6fa786"))
				.settings(PageSettings.builder()
						.name("Seite X")
						.build())
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
						.settings(PageSettings.builder()
								.name("Seite X")
								.build())
						.build(),
				1);


		assertThat(project.getPages()).hasSize(3);
		assertThat(project.getPages().get(0)).extracting(Page::getId, Page::getPosition).containsExactly(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 0);
		assertThat(project.getPages().get(1)).extracting(Page::getId, Page::getPosition).containsExactly(UUID.fromString("4480bbf8-ef96-4592-97e3-cc9a2b6fa786"), 1);
		assertThat(project.getPages().get(2)).extracting(Page::getId, Page::getPosition).containsExactly(addedPage.getId(), 2);
	}

	@Test
	void test_updateNumberOfPadsPerRowAndColumn_equalSize()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_5.json");
		final ProjectMetadata oldMetadata = project.getMetadata().copy(false);

		projectService.updateNumberOfPadsPerRowAndColumn(project, oldMetadata);

		assertThat(project.getPages()).hasSize(1);
		assertThat(project.getPages().getFirst().getPads())
				.extracting(Pad::getName, Pad::getPosition)
				.containsExactly(tuple("Test Pad 1", 0),
						tuple("Test Pad 2", 1),
						tuple("Test Pad 3", 2),
						tuple("Test Pad 4", 3));
	}

	@Test
	void test_updateNumberOfPadsPerRowAndColumn_newRows()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_5.json");
		final ProjectMetadata oldMetadata = project.getMetadata().copy(false);

		project.getMetadata().setNumberOfVerticalPads(4);
		projectService.updateNumberOfPadsPerRowAndColumn(project, oldMetadata);

		assertThat(project.getPages()).hasSize(1);
		assertThat(project.getPages().getFirst().getPads())
				.extracting(Pad::getName, Pad::getPosition)
				.containsExactly(tuple("Test Pad 1", 0),
						tuple("Test Pad 2", 1),
						tuple("Test Pad 3", 2),
						tuple("Test Pad 4", 3),
						tuple(null, 4),
						tuple(null, 5),
						tuple(null, 6),
						tuple(null, 7));
	}

	@Test
	void test_updateNumberOfPadsPerRowAndColumn_newColumns()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_5.json");
		final ProjectMetadata oldMetadata = project.getMetadata().copy(false);

		project.getMetadata().setNumberOfHorizontalPads(4);
		projectService.updateNumberOfPadsPerRowAndColumn(project, oldMetadata);

		assertThat(project.getPages()).hasSize(1);
		assertThat(project.getPages().getFirst().getPads())
				.extracting(Pad::getName, Pad::getPosition)
				.containsExactly(tuple("Test Pad 1", 0),
						tuple("Test Pad 2", 1),
						tuple(null, 2),
						tuple(null, 3),
						tuple("Test Pad 3", 4),
						tuple("Test Pad 4", 5),
						tuple(null, 6),
						tuple(null, 7));
	}

	@Test
	void test_updateNumberOfPadsPerRowAndColumn_newRowsAndColumns()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_5.json");
		final ProjectMetadata oldMetadata = project.getMetadata().copy(false);

		project.getMetadata().setNumberOfHorizontalPads(4);
		project.getMetadata().setNumberOfVerticalPads(4);
		projectService.updateNumberOfPadsPerRowAndColumn(project, oldMetadata);

		assertThat(project.getPages()).hasSize(1);
		assertThat(project.getPages().getFirst().getPads())
				.extracting(Pad::getName, Pad::getPosition)
				.containsExactly(tuple("Test Pad 1", 0),
						tuple("Test Pad 2", 1),
						tuple(null, 2),
						tuple(null, 3),
						tuple("Test Pad 3", 4),
						tuple("Test Pad 4", 5),
						tuple(null, 6),
						tuple(null, 7),
						tuple(null, 8),
						tuple(null, 9),
						tuple(null, 10),
						tuple(null, 11),
						tuple(null, 12),
						tuple(null, 13),
						tuple(null, 14),
						tuple(null, 15));
	}

	@Test
	void test_updateNumberOfPadsPerRowAndColumn_removeRow()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_5.json");
		final ProjectMetadata oldMetadata = project.getMetadata().copy(false);

		project.getMetadata().setNumberOfVerticalPads(1);
		projectService.updateNumberOfPadsPerRowAndColumn(project, oldMetadata);

		assertThat(project.getPages()).hasSize(1);
		assertThat(project.getPages().getFirst().getPads())
				.extracting(Pad::getName, Pad::getPosition)
				.containsExactly(tuple("Test Pad 1", 0),
						tuple("Test Pad 2", 1));
	}

	@Test
	void test_updateNumberOfPadsPerRowAndColumn_removeColumn()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_5.json");
		final ProjectMetadata oldMetadata = project.getMetadata().copy(false);

		project.getMetadata().setNumberOfHorizontalPads(1);
		projectService.updateNumberOfPadsPerRowAndColumn(project, oldMetadata);

		assertThat(project.getPages()).hasSize(1);
		assertThat(project.getPages().getFirst().getPads())
				.extracting(Pad::getName, Pad::getPosition)
				.containsExactly(tuple("Test Pad 1", 0),
						tuple("Test Pad 3", 1));
	}

	@Test
	void test_updateNumberOfPadsPerRowAndColumn_removeRowAndColumn()
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_5.json");
		final ProjectMetadata oldMetadata = project.getMetadata().copy(false);

		project.getMetadata().setNumberOfHorizontalPads(1);
		project.getMetadata().setNumberOfVerticalPads(1);
		final List<UUID> removedPads = projectService.updateNumberOfPadsPerRowAndColumn(project, oldMetadata);

		assertThat(project.getPages()).hasSize(1);
		assertThat(project.getPages().getFirst().getPads())
				.extracting(Pad::getName, Pad::getPosition)
				.containsExactly(tuple("Test Pad 1", 0));
		assertThat(removedPads).containsExactly(
				UUID.fromString("895082d5-3655-4aca-96db-818fef99e9ef"),
				UUID.fromString("f55f7691-2842-4d3f-9b64-08ddb0161398"),
				UUID.fromString("c37d6bb6-49a7-4b72-964d-dc9a8571507a")
		);
	}

	@Test
	void test_importProject() throws URISyntaxException, IOException
	{
		final Path projectPath = Paths.get(Objects.requireNonNull(ProjectServiceTest.class.getClassLoader().getResource("projects/project_1.json")).toURI());
		final byte[] bytes = Files.readAllBytes(projectPath);

		final UUID uuid = projectService.importProject("application/json", bytes);
		assertThat(uuid).isNotNull();
	}

	@SuppressWarnings("java:S5976")
	@Test
	void test_importProject_noProject() throws URISyntaxException, IOException
	{
		final Path projectPath = Paths.get(Objects.requireNonNull(ProjectServiceTest.class.getClassLoader().getResource("projects/no_project.json")).toURI());
		final byte[] bytes = Files.readAllBytes(projectPath);

		assertThatThrownBy(() -> projectService.importProject("application/json", bytes))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Die ausgewählte Datei ist keine gültige PlayWall Projektdatei");
	}

	@Test
	void test_importProject_unsupportedVersion() throws URISyntaxException, IOException
	{
		final Path projectPath = Paths.get(Objects.requireNonNull(ProjectServiceTest.class.getClassLoader().getResource("projects/project_unsupported.json")).toURI());
		final byte[] bytes = Files.readAllBytes(projectPath);

		assertThatThrownBy(() -> projectService.importProject("application/json", bytes))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Die ausgewählte Datei ist keine gültige PlayWall Projektdatei");
	}

	@Test
	void test_importProject_versionTooOld() throws URISyntaxException, IOException
	{
		final Path projectPath = Paths.get(Objects.requireNonNull(ProjectServiceTest.class.getClassLoader().getResource("projects/project_too_old.json")).toURI());
		final byte[] bytes = Files.readAllBytes(projectPath);

		assertThatThrownBy(() -> projectService.importProject("application/json", bytes))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Die Projektdatei ist zu alt. Version: 0 Mindestversion: 1");
	}
}
