package de.tobias.playwall.server.api.project.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import de.tobias.playwall.common.api.project.request.ProjectSettingsUpdateRequest;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.RequestHandlerFactory;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import de.tobias.playwall.server.project.ProjectController;
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
import java.util.Optional;

import static de.tobias.playwall.server.api.project.handler.UndoTestHelper.testInverseOperation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class ProjectSettingsUpdateHandlerTest
{
	@TempDir
	private Path tempDir;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private RequestHandlerFactory requestHandlerFactory;

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
				""");

		when(pathProvider.getPathForConfig(any())).thenReturn(projectsFile);
		projectService.getAllProjectMetadata();
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
				.build());
		final Optional<ResponseMessage> responseMessage = handler.handleRequest(request);

		assertThat(responseMessage).isEmpty();

		assertThat(project.getMetadata().getName()).isEqualTo("Fancy project name");

		assertThat(applicationEvents.stream(ProjectSettingsUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getProjectMetadata().name()).isEqualTo("Fancy project name"));
	}

	@Test
	void testPadSettingsUpdateHandlerProjectNotLoaded()
	{
		final ProjectSettingsUpdateRequest request = new ProjectSettingsUpdateRequest(ProjectMetadataDto.builder()
				.name("Fancy project name")
				.build());

		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotLoadedException.class);

		assertThat(applicationEvents.stream(PadUpdate.class)).isEmpty();
	}

	@Test
	void testProjectSettingsUpdateHandlerSameProjectNameForCurrentProjectOK() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final ProjectSettingsUpdateRequest request = new ProjectSettingsUpdateRequest(ProjectMetadataDto.builder()
				.name("Project 1")
				.build());
		final Optional<ResponseMessage> responseMessage = handler.handleRequest(request);

		assertThat(responseMessage).isEmpty();

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
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(ProjectNameAlreadyExistsError.class);

		assertThat(applicationEvents.stream(PadUpdate.class)).isEmpty();
		assertThat(project.getMetadata().getName()).isEqualTo("Project 1");
	}

	@Test
	void testUndoOperation() throws Exception
	{
		final ProjectSettingsUpdateRequest request = new ProjectSettingsUpdateRequest(ProjectMetadataDto.builder()
				.name("Fancy project name")
				.build());

		testInverseOperation(objectMapper, projectController, applicationEvents, handler, request, requestHandlerFactory);
	}
}
