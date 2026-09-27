package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.BatchReplaceMediaRequest;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.config.SyncAsyncConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
@Import(SyncAsyncConfig.class)
@ExtendWith(MockitoExtension.class)
class BatchReplaceMediaHandlerTest extends AbstractUndoableRequestHandlerTest<BatchReplaceMediaRequest>
{
	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private BatchReplaceMediaHandler handler;

	@BeforeEach
	void init()
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);
	}

	@Test
	void testBatchReplaceMediaAllNew() throws Exception
	{
		final UUID padId1 = UUID.fromString("4f05c367-d77b-4b0d-acee-1fd6bb6e6de9");
		final UUID padId2 = UUID.fromString("ed012839-da8e-4654-a635-1e6daa36f469");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_7.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final Path mediaPath1 = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI());
		final Path mediaPath2 = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_2.mp3")).toURI());

		final BatchReplaceMediaRequest request = new BatchReplaceMediaRequest(Map.of(
				padId1, mediaPath1.toAbsolutePath().toString(),
				padId2, mediaPath2.toAbsolutePath().toString()),
				Set.of());
		handler.handleRequest(request);

		assertThat(project.getPad(padId1).getName()).isEqualTo("example_1");
		assertThat(project.getPad(padId1).getContent()).isInstanceOf(AudioPadContent.class);
		assertThat(((AudioPadContent) project.getPad(padId1).getContent()).getMediaPath()).endsWith("example_1.mp3");

		assertThat(project.getPad(padId2).getName()).isEqualTo("example_2");
		assertThat(project.getPad(padId2).getContent()).isInstanceOf(AudioPadContent.class);
		assertThat(((AudioPadContent) project.getPad(padId2).getContent()).getMediaPath()).endsWith("example_2.mp3");

		assertThat(applicationEvents.stream(PadUpdate.class)).hasSize(2);
	}

	@Test
	void testBatchReplaceMediaMixed() throws Exception
	{
		final UUID padId1 = UUID.fromString("4f05c367-d77b-4b0d-acee-1fd6bb6e6de9");
		final UUID padId2 = UUID.fromString("ed012839-da8e-4654-a635-1e6daa36f469");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_7.json");
		final Path existingMedia = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_2.mp3")).toURI());
		project.getPad(padId2).setContent(AudioPadContent.builder().mediaPath(existingMedia.toAbsolutePath().toString().replace("\\", "/")).loop(false).build());
		project.getPad(padId2).setName("example_2");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final Path mediaPath1 = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI());

		final BatchReplaceMediaRequest request = new BatchReplaceMediaRequest(Map.of(
				padId1, mediaPath1.toAbsolutePath().toString()),
				Set.of(padId2)
		);
		handler.handleRequest(request);

		assertThat(project.getPad(padId1).getName()).isEqualTo("example_1");
		assertThat(project.getPad(padId1).getContent()).isInstanceOf(AudioPadContent.class);

		assertThat(project.getPad(padId2).getContent()).isNull();

		assertThat(applicationEvents.stream(PadUpdate.class)).hasSize(2);
	}

	@Test
	void testUndoOperation() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_7.json");

		final Path mediaPath1 = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI());

		final BatchReplaceMediaRequest request = new BatchReplaceMediaRequest(Map.of(
				UUID.fromString("4f05c367-d77b-4b0d-acee-1fd6bb6e6de9"), mediaPath1.toAbsolutePath().toString()),
				Set.of(UUID.fromString("ed012839-da8e-4654-a635-1e6daa36f469"))
		);

		testInverseOperation(project, request);
	}
}
