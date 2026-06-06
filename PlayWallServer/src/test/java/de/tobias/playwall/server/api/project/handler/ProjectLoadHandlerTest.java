package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.common.TimeMode;
import de.tobias.playwall.common.api.pad.update.PadLoadedUpdate;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.api.project.request.ProjectLoadRequest;
import de.tobias.playwall.common.api.project.request.ProjectSettingsUpdateRequest;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.api.history.UndoManager;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import de.tobias.playwall.server.config.SyncAsyncConfig;
import de.tobias.playwall.server.net.RequestExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
@Import(SyncAsyncConfig.class)
class ProjectLoadHandlerTest extends AbstractRequestHandlerTest
{
	@TempDir
	private Path tempDir;

	@MockitoBean
	private PathProvider pathProvider;

	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private JsonMapper objectMapper;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectLoadHandler handler;

	@Autowired
	private RequestExecutor requestExecutor;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private AllProjectsInfoRepository allProjectsInfoRepository;

	@Autowired
	private UndoManager undoManager;

	@BeforeEach
	void init() throws IOException
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
		allProjectsInfoRepository.loadAllProjectsInfo();
	}

	@Test
	void testProjectLoadRequestSuccessful() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();
		project.getPad(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616")).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());

		final UUID projectId = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		when(projectRepository.loadProject(projectId)).thenReturn(project);

		handler.handleRequest(new ProjectLoadRequest(projectId));

		assertThat(applicationEvents.stream(PadLoadedUpdate.class)).hasSize(2);

		assertThat(allProjectsInfoRepository.getRecentProjectIds()).containsExactly(projectId);

		assertThat(undoManager.getUndoOperation()).isNull();
	}

	@Test
	void testProjectLoadRequestNotFound() throws Exception
	{
		final UUID projectId = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		when(projectRepository.loadProject(projectId)).thenThrow(new ProjectNotExistsException(projectId));

		final ProjectLoadRequest request = new ProjectLoadRequest(projectId);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void testProjectLoadRequestCheckUndoManagerIsCleared() throws Exception
	{
		final Project project1 = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final UUID projectId1 = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		when(projectRepository.loadProject(projectId1)).thenReturn(project1);

		final Project project2 = TestUtils.loadProject(objectMapper, "projects/project_2.json");
		final UUID projectId2 = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		when(projectRepository.loadProject(projectId2)).thenReturn(project2);

		// load first project
		handler.handleRequest(new ProjectLoadRequest(projectId1));

		// create an undoable action
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

		requestExecutor.execute(request);

		// load second project
		handler.handleRequest(new ProjectLoadRequest(projectId2));

		assertThat(undoManager.getUndoOperation()).isNull();
	}
}
