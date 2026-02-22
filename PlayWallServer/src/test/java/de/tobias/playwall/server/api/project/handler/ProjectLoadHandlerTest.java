package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.pad.update.PadLoadedUpdate;
import de.tobias.playwall.common.api.project.request.ProjectLoadRequest;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import de.tobias.playwall.server.config.SyncAsyncConfig;
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
class ProjectLoadHandlerTest
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

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private AllProjectsInfoRepository allProjectsInfoRepository;

	@BeforeEach
	void init() throws IOException
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);

		when(pathProvider.getPathForConfig(any())).thenReturn(tempDir.resolve("projects.json"));
		Files.deleteIfExists(tempDir.resolve("projects.json"));
		allProjectsInfoRepository.loadAllProjectsInfo();
	}

	@Test
	void testProjectGetRequestSuccessful() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();
		project.getPad(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616")).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());

		final UUID projectId = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		when(projectRepository.loadProject(projectId)).thenReturn(project);

		handler.handleRequest(new ProjectLoadRequest(projectId));

		assertThat(applicationEvents.stream(PadLoadedUpdate.class)).hasSize(2);

		assertThat(allProjectsInfoRepository.getRecentProjectIds()).containsExactly(projectId);
	}

	@Test
	void testProjectGetRequestNotFound() throws Exception
	{
		final UUID projectId = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		when(projectRepository.loadProject(projectId)).thenThrow(new ProjectNotExistsException(projectId));

		final ProjectLoadRequest request = new ProjectLoadRequest(projectId);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotExistsException.class);
	}
}
