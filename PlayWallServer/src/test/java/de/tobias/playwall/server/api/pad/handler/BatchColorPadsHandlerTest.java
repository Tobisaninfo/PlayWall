package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.pad.request.BatchColorPadsRequest;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class BatchColorPadsHandlerTest extends AbstractUndoableRequestHandlerTest<BatchColorPadsRequest>
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
	private BatchColorPadsHandler handler;

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
	void testBatchColorPadsRequest() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_3.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final Map<UUID, Color> padColors = Map.of(
				UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"), Color.DARK_RED1,
				UUID.fromString("efb30a6f-593b-4a15-94db-faa2d4117e4f"), Color.LIGHT_GREEN2
		);

		final BatchColorPadsRequest request = new BatchColorPadsRequest(padColors);
		handler.handleRequest(request);

		assertThat(applicationEvents.stream(PadUpdate.class)).hasSize(2);
		assertThat(applicationEvents.stream(PadUpdate.class).toList().get(0).getPad().getDefaultColor()).isEqualTo(Color.DARK_RED1);
		assertThat(applicationEvents.stream(PadUpdate.class).toList().get(1).getPad().getDefaultColor()).isEqualTo(Color.LIGHT_GREEN2);
	}


	@Test
	void testUndoOperation() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_3.json");

		final Map<UUID, Color> padColors = Map.of(
				UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"), Color.DARK_RED1,
				UUID.fromString("efb30a6f-593b-4a15-94db-faa2d4117e4f"), Color.LIGHT_GREEN2
		);

		final BatchColorPadsRequest request = new BatchColorPadsRequest(padColors);
		testInverseOperation(project, request);
	}
}
