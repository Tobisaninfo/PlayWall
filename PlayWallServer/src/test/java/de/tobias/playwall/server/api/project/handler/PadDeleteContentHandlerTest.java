package de.tobias.playwall.server.api.project.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.project.PadDeleteContentRequest;
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
class PadDeleteContentHandlerTest
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
	private PadDeleteContentHandler handler;

	@BeforeEach
	void init()
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);
	}

	@Test
	void testPadDeleteContentHandlerOnEmptyPad() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final PadDeleteContentRequest request = new PadDeleteContentRequest(padId);
		final Optional<ResponseMessage> responseMessage = handler.handleRequest(request);

		assertThat(responseMessage).isEmpty();

		assertThat(project.getPad(padId).getContent()).isNull();

		assertThat(applicationEvents.stream(PadUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getPad().getContent()).isNull());
	}

	@Test
	void testPadNewMediaHandlerOnAudioPad() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_2.mp3")).toURI()).toAbsolutePath().toString();
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(true).build());
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final PadDeleteContentRequest request = new PadDeleteContentRequest(padId);
		final Optional<ResponseMessage> responseMessage = handler.handleRequest(request);

		assertThat(responseMessage).isEmpty();

		assertThat(project.getPad(padId).getContent()).isNull();

		assertThat(applicationEvents.stream(PadUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getPad().getContent()).isNull());
	}

	@Test
	void testPadNewMediaHandlerPadNotFound() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2d55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final PadDeleteContentRequest request = new PadDeleteContentRequest(padId);

		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(PadNotExistsError.class);

		assertThat(applicationEvents.stream(PadUpdate.class)).isEmpty();
	}
}
