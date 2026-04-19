package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.common.TimeMode;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.api.project.request.ProjectSettingsUpdateRequest;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import de.tobias.playwall.common.api.project.update.ProjectUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.project.ProjectNameAlreadyExistsException;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class ProjectSettingsUpdateHandlerTest extends AbstractUndoableRequestHandlerTest<ProjectSettingsUpdateRequest>
{
	@TempDir
	private Path tempDir;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectSettingsUpdateHandler handler;

	@MockitoBean
	private PathProvider pathProvider;

	@BeforeEach
	void beforeEach() throws IOException
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);

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
	void testProjectSettingsUpdateHandler() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final ProjectSettingsUpdateRequest request = new ProjectSettingsUpdateRequest(ProjectMetadataDto.builder()
				.name("Fancy project name")
				.numberOfHorizontalPads(3)
				.numberOfVerticalPads(5)
				.volume(1.0)
				.timeMode(TimeMode.ELAPSED_AND_TOTAL)
				.defaultColor(Color.DARK_RED1)
				.playColor(Color.BLUE1)
				.introColor(Color.LIGHT_GREEN2)
				.build());
		handler.handleRequest(request);

		assertThat(project.getMetadata()).extracting(ProjectMetadata::getName, ProjectMetadata::getTimeMode, ProjectMetadata::getDefaultColor, ProjectMetadata::getPlayColor, ProjectMetadata::getIntroColor, ProjectMetadata::getNumberOfHorizontalPads, ProjectMetadata::getNumberOfVerticalPads)
				.containsExactly("Fancy project name", TimeMode.ELAPSED_AND_TOTAL, Color.DARK_RED1, Color.BLUE1, Color.LIGHT_GREEN2, 3, 5);

		assertThat(applicationEvents.stream(ProjectSettingsUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getProjectMetadata())
						.extracting(ProjectMetadataDto::name, ProjectMetadataDto::timeMode, ProjectMetadataDto::defaultColor, ProjectMetadataDto::playColor, ProjectMetadataDto::introColor, ProjectMetadataDto::numberOfHorizontalPads, ProjectMetadataDto::numberOfVerticalPads)
						.containsExactly("Fancy project name", TimeMode.ELAPSED_AND_TOTAL, Color.DARK_RED1, Color.BLUE1, Color.LIGHT_GREEN2, 3, 5));

		assertThat(applicationEvents.stream(ProjectUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getProject().pages().getFirst().pads()).hasSize(15))
		;
	}

	@Test
	void testPadSettingsUpdateHandlerProjectNotLoaded()
	{
		final ProjectSettingsUpdateRequest request = new ProjectSettingsUpdateRequest(ProjectMetadataDto.builder()
				.name("Fancy project name")
				.build());

		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotLoadedException.class);

		assertThat(applicationEvents.stream(ProjectSettingsUpdate.class)).isEmpty();
	}

	@Test
	void testProjectSettingsUpdateHandlerSameProjectNameForCurrentProjectOK() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final ProjectSettingsUpdateRequest request = new ProjectSettingsUpdateRequest(ProjectMetadataDto.builder()
				.name("Project 1")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(4)
				.volume(1.0)
				.timeMode(TimeMode.ELAPSED)
				.defaultColor(Color.GRAY1)
				.playColor(Color.RED3)
				.eofWarningTime(2.0)
				.build());
		handler.handleRequest(request);

		assertThat(project.getMetadata().getName()).isEqualTo("Project 1");

		assertThat(applicationEvents.stream(ProjectSettingsUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getProjectMetadata().name()).isEqualTo("Project 1"));
	}

	@Test
	void testProjectSettingsUpdateHandlerProjectNameAlreadyExists() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final ProjectSettingsUpdateRequest request = new ProjectSettingsUpdateRequest(ProjectMetadataDto.builder()
				.name("Project 2")
				.build());

		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNameAlreadyExistsException.class);

		assertThat(applicationEvents.stream(ProjectSettingsUpdate.class)).isEmpty();
		assertThat(project.getMetadata().getName()).isEqualTo("Project 1");
	}

	@Test
	void testUndoOperation() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");

		final ProjectSettingsUpdateRequest request = new ProjectSettingsUpdateRequest(ProjectMetadataDto.builder()
				.name("Fancy Project Name")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(4)
				.volume(1.0)
				.timeMode(TimeMode.ELAPSED)
				.defaultColor(Color.GRAY1)
				.playColor(Color.RED3)
				.eofWarningTime(2.0)
				.build());

		testInverseOperation(project, request);
	}

	@Test
	void testUndoOperationNumberOfPadsChanged() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");

		final ProjectSettingsUpdateRequest request = new ProjectSettingsUpdateRequest(ProjectMetadataDto.builder()
				.name("Fancy Project Name")
				.numberOfHorizontalPads(3)
				.numberOfVerticalPads(5)
				.volume(1.0)
				.timeMode(TimeMode.ELAPSED)
				.defaultColor(Color.GRAY1)
				.playColor(Color.RED3)
				.build());

		testInverseOperation(project, request);
	}
}
