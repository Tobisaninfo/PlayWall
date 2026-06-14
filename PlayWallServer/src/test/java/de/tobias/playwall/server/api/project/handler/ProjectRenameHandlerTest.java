package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectRenameRequest;
import de.tobias.playwall.common.api.project.request.ProjectSettingsUpdateRequest;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectNameAlreadyExistsException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class ProjectRenameHandlerTest extends AbstractUndoableRequestHandlerTest<ProjectSettingsUpdateRequest>
{
	private static final UUID project1 = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");

	@Autowired
	private ApplicationEvents applicationEvents;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectRenameHandler handler;

	@MockitoBean
	private AllProjectsInfoRepository allProjectsInfoRepository;

	@Captor
	private ArgumentCaptor<Project> projectCaptor;

	@BeforeEach
	void beforeEach()
	{
		when(allProjectsInfoRepository.getAllProjectMetadata())
				.thenReturn(List.of(
						new ProjectMetadata(UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c"), "Project 1"),
						new ProjectMetadata(UUID.fromString("14bd0090-6322-4133-966d-b78296565a7f"), "Project 2")
				));
		when(allProjectsInfoRepository.getProjectMetadataByName(any())).thenCallRealMethod();

		projectController.unloadProject();
	}

	@Test
	void testProjectRenameHandlerSameProjectNameForCurrentProjectOK() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		when(projectRepository.loadProject(any())).thenReturn(project);

		projectController.loadProject(project).get();
		applicationEvents.clear();

		final ProjectRenameRequest request = new ProjectRenameRequest(project1, "Project 3");

		handler.handleRequest(request);

		assertThat(applicationEvents.stream(ProjectSettingsUpdate.class)).hasSize(1)
				.first()
				.satisfies(projectSettingsUpdate -> assertThat(projectSettingsUpdate.getProjectMetadata().name()).isEqualTo("Project 3"));
		assertThat(project.getMetadata().getName()).isEqualTo("Project 3");

		verify(projectRepository).saveProject(projectCaptor.capture());
		assertThat(projectCaptor.getValue().getMetadata().getName()).isEqualTo("Project 3");
	}

	@Test
	void testProjectRenameHandlerClosedProject() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		when(projectRepository.loadProject(any())).thenReturn(project);

		final ProjectRenameRequest request = new ProjectRenameRequest(project1, "Project 3");

		handler.handleRequest(request);

		assertThat(applicationEvents.stream(ProjectSettingsUpdate.class)).isEmpty();

		verify(projectRepository).saveProject(projectCaptor.capture());
		assertThat(projectCaptor.getValue().getMetadata().getName()).isEqualTo("Project 3");
	}

	@Test
	void testProjectRenameHandlerProjectNameAlreadyExists() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		when(projectRepository.loadProject(any())).thenReturn(project);

		projectController.loadProject(project).get();
		applicationEvents.clear();

		final ProjectRenameRequest request = new ProjectRenameRequest(project1, "Project 2");

		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNameAlreadyExistsException.class);

		assertThat(applicationEvents.stream(ProjectSettingsUpdate.class)).isEmpty();
		assertThat(project.getMetadata().getName()).isEqualTo("Project 1");
	}
}
