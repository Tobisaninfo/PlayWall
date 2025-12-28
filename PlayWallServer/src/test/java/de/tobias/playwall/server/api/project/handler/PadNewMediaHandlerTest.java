package de.tobias.playwall.server.api.project.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.project.PadLoadedUpdate;
import de.tobias.playwall.common.api.project.PadNewMediaRequest;
import de.tobias.playwall.common.api.project.PadNotExistsError;
import de.tobias.playwall.common.api.project.PadUpdate;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.config.SyncAsyncConfig;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
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
class PadNewMediaHandlerTest
{
	@Autowired
	private ApplicationEvents applicationEvents;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private PadNewMediaHandler handler;

	@BeforeEach
	void init()
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);
	}

	@Test
	void testPadNewMediaHandlerOnEmptyPad() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final Path mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI());
		final PadNewMediaRequest request = new PadNewMediaRequest(padId, mediaPath.toAbsolutePath().toString());
		final Optional<ResponseMessage> responseMessage = handler.handleRequest(request);

		assertThat(responseMessage).isEmpty();

		assertThat(project.getPad(padId).getName()).isEqualTo("example_1");
		assertThat(project.getPad(padId).getContent()).isInstanceOf(AudioPadContent.class);
		assertThat(((AudioPadContent) project.getPad(padId).getContent()).getMediaPath()).endsWith("example_1.mp3");
		assertThat(((AudioPadContent) project.getPad(padId).getContent()).isLoop()).isFalse();

		assertThat(applicationEvents.stream(PadUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getPad().getName()).isEqualTo("example_1"));
		assertThat(applicationEvents.stream(PadLoadedUpdate.class))
				.hasSize(2)
				.allSatisfy(event -> assertThat(event.getPadId()).isEqualTo(padId));
	}

	@Test
	void testPadNewMediaHandlerOnAudioPad() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final String oldMediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_2.mp3")).toURI()).toAbsolutePath().toString();
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(oldMediaPath).loop(true).build());
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final Path mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI());
		final PadNewMediaRequest request = new PadNewMediaRequest(padId, mediaPath.toAbsolutePath().toString());
		final Optional<ResponseMessage> responseMessage = handler.handleRequest(request);

		assertThat(responseMessage).isEmpty();

		assertThat(project.getPad(padId).getName()).isEqualTo("example_1");
		assertThat(project.getPad(padId).getContent()).isInstanceOf(AudioPadContent.class);
		assertThat(((AudioPadContent) project.getPad(padId).getContent()).getMediaPath()).endsWith("example_1.mp3");
		assertThat(((AudioPadContent) project.getPad(padId).getContent()).isLoop()).isTrue();

		assertThat(applicationEvents.stream(PadUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getPad().getName()).isEqualTo("example_1"));
		assertThat(applicationEvents.stream(PadLoadedUpdate.class))
				.hasSize(2)
				.allSatisfy(event -> assertThat(event.getPadId()).isEqualTo(padId));
	}

	@Test
	void testPadNewMediaHandlerPadNotFound() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2d55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final Path mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI());
		final PadNewMediaRequest request = new PadNewMediaRequest(padId, mediaPath.toAbsolutePath().toString());
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(PadNotExistsError.class);

		assertThat(applicationEvents.stream(PadUpdate.class)).isEmpty();
		assertThat(applicationEvents.stream(PadLoadedUpdate.class)).isEmpty();
	}
}
