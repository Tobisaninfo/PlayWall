package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.project.request.ProjectGetCurrentRequest;
import de.tobias.playwall.common.api.project.request.ProjectGetCurrentResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectGetCurrentHandlerTest extends AbstractRequestHandlerTest
{
	@TempDir
	private Path tempDir;

	@Autowired
	private JsonMapper objectMapper;

	@MockitoBean
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private ProjectGetCurrentHandler handler;

	@MockitoBean
	private PathProvider pathProvider;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@BeforeEach
	void init() throws IOException
	{
		final Path projectsFile = tempDir.resolve("projects.json");

		Files.writeString(projectsFile, "[]");

		when(pathProvider.getPathForConfig(any())).thenReturn(projectsFile);
		projectController.unloadProject();

		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);
	}

	@Test
	void testProjectGetCurrentRequestReturnsLoadedProject() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final UUID projectId = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");
		projectController.loadProject(project).get();
		projectController.setCurrentPageIndex(2);

		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectGetCurrentRequest());

		assertThat(response).isNotEmpty();
		final ProjectGetCurrentResponse projectGetResponse = (ProjectGetCurrentResponse) response.get();
		assertThat(projectGetResponse.getProject().metadata().id()).isEqualTo(projectId);
		assertThat(projectGetResponse.getCurrentPageIndex()).isEqualTo(2);
	}

	@Test
	void testProjectGetCurrentRequestReturnsPadStatuses() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_6_1x1.json");
		projectController.loadProject(project).get();
		final Pad pad = project.getPad(UUID.fromString("3a9edcfd-c4a0-48c5-a34b-d2b9cdc12225"));
		final PadController padController = projectController.createNewPadController(pad);
		padController.setStatus(PadControllerStatus.PAUSED);

		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectGetCurrentRequest());

		assertThat(response).isNotEmpty();
		assertThat(((ProjectGetCurrentResponse) response.get()).getPadStatusById()).containsExactlyInAnyOrderEntriesOf(
				Map.of(UUID.fromString("3a9edcfd-c4a0-48c5-a34b-d2b9cdc12225"), PadControllerStatus.PAUSED,
						UUID.fromString("0a589aed-3ead-4fe0-9c0a-4c7b1b83b8a6"), PadControllerStatus.READY)
		);
	}

	@Test
	void testProjectGetCurrentRequestWithNoProjectLoaded()
	{
		final ProjectGetCurrentRequest request = new ProjectGetCurrentRequest();
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotLoadedException.class);
	}
}
