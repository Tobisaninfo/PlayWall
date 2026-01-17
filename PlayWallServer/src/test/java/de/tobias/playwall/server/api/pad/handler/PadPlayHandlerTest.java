package de.tobias.playwall.server.api.pad.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.project.PadNotExistsError;
import de.tobias.playwall.common.api.project.PadPlayRequest;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PadPlayHandlerTest
{
	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private PadPlayHandler handler;

	private final AudioHandler audioHandler = mock(AudioHandler.class);

	@BeforeEach
	void init()
	{
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);
	}

	@Test
	void testPadPauseHandlerOnExistingPad() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());
		projectController.loadProject(project).get();

		final Optional<ResponseMessage> response = handler.handleRequest(new PadPlayRequest(padId));

		assertThat(response).isEmpty();
		verify(audioHandler).play();
	}

	@Test
	void testPadPauseHandlerOnNotExistingPad() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8448-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		assertThatThrownBy(() -> handler.handleRequest(new PadPlayRequest(padId)))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(PadNotExistsError.class);

		verify(audioHandler, never()).play();
	}
}
