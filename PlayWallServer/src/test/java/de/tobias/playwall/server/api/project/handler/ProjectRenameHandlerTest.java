package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectRenameRequest;
import de.tobias.playwall.common.api.project.request.ProjectSettingsUpdateRequest;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectNameAlreadyExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class ProjectRenameHandlerTest extends AbstractUndoableRequestHandlerTest<ProjectSettingsUpdateRequest>
{
	private static final UUID project1 = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");

	@TempDir
	private Path tempDir;

	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectRenameHandler handler;

	@Autowired
	private AllProjectsInfoRepository allProjectsInfoRepository;

	@MockitoBean
	private PathProvider pathProvider;

	@BeforeEach
	void beforeEach() throws IOException
	{
		final Path projectsFile = tempDir.resolve("projects.json");

		Files.writeString(projectsFile, """
				{
					"recentProjects": [],
					"allProjectsMetadata":
					[
						 {
							 "id": "a09d1f3c-2384-4ee5-b13d-07f428efe35c",
							 "name": "Project 1"
						 },
						  {
							 "id": "14bd0090-6322-4133-966d-b78296565a7f",
							 "name": "Project 2"
						 }
					 ]
				 }
				""");

		when(pathProvider.getPathForConfig(any())).thenReturn(projectsFile);
		projectService.getAllProjectsInfo();
		projectController.unloadProject();
	}

	@Test
	void testProjectRenameHandlerSameProjectNameForCurrentProjectOK() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final ProjectRenameRequest request = new ProjectRenameRequest(project1, "Project 3");

		handler.handleRequest(request);

		assertThat(applicationEvents.stream(ProjectSettingsUpdate.class)).hasSize(1)
				.first()
				.satisfies(projectSettingsUpdate -> assertThat(projectSettingsUpdate.getProjectMetadata().name()).isEqualTo("Project 3"));
		assertThat(project.getMetadata().getName()).isEqualTo("Project 3");
	}

	// TODO
//	@Test
//	void testProjectRenameHandlerClosedProject() throws Exception
//	{
//		final ProjectRenameRequest request = new ProjectRenameRequest(project1, "Project 3");
//
//		handler.handleRequest(request);
//
//		assertThat(applicationEvents.stream(ProjectSettingsUpdate.class)).isEmpty();
//
//		assertThat(allProjectsInfoRepository.getAllProjects())
//				.filteredOn(projectMetadata -> projectMetadata.getId().equals(project1)).first()
//				.satisfies(projectMetadata -> assertThat(projectMetadata.getName()).isEqualTo("Project 3"));
//	}

	@Test
	void testProjectRenameHandlerProjectNameAlreadyExists() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final ProjectRenameRequest request = new ProjectRenameRequest(project1, "Project 2");

		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNameAlreadyExistsException.class);

		assertThat(applicationEvents.stream(ProjectSettingsUpdate.class)).isEmpty();
		assertThat(project.getMetadata().getName()).isEqualTo("Project 1");
	}
}
