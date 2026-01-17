package de.tobias.playwall.server.api.pad.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.pad.request.PadChangeVolumeRequest;
import de.tobias.playwall.common.api.pad.request.PadNotExistsError;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
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
import org.mockito.Mock;
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
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
@Import(SyncAsyncConfig.class)
class PadChangeVolumeHandlerTest
{
	@Autowired
	private ApplicationEvents applicationEvents;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Mock
	private AudioHandler audioHandler;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private PadChangeVolumeHandler handler;

	@BeforeEach
	void init()
	{
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);
	}

	@Test
	void testPadChangeVolumeHandler() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_2.mp3")).toURI()).toAbsolutePath().toString();
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).volume(1.0).build());
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final PadChangeVolumeRequest request = new PadChangeVolumeRequest(padId, 0.25);
		final Optional<ResponseMessage> responseMessage = handler.handleRequest(request);

		assertThat(responseMessage).isEmpty();

		assertThat(project.getPad(padId).getContent()).isInstanceOf(AudioPadContent.class)
				.satisfies(padContent -> assertThat(((AudioPadContent) padContent).getVolume()).isEqualTo(1.0)); // Not persisted
		verify(audioHandler).setVolume(0.25);
		assertThat(applicationEvents.stream(PadUpdate.class)).isEmpty();
	}

	@Test
	void testPadChangeVolumeHandlerGlobalVolumeShouldHaveAnEffect() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		project.getMetadata().setVolume(0.5);
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_2.mp3")).toURI()).toAbsolutePath().toString();
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).volume(1.0).build());
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final PadChangeVolumeRequest request = new PadChangeVolumeRequest(padId, 0.25);
		final Optional<ResponseMessage> responseMessage = handler.handleRequest(request);

		assertThat(responseMessage).isEmpty();

		verify(audioHandler).setVolume(0.125);
		assertThat(applicationEvents.stream(PadUpdate.class)).isEmpty();
	}

	@Test
	void testPadChangeVolumeHandlerPadNotFound() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2d55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final PadChangeVolumeRequest request = new PadChangeVolumeRequest(padId, 0.25);

		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(PadNotExistsError.class);
		verify(audioHandler, never()).setVolume(0.25);
		assertThat(applicationEvents.stream(PadUpdate.class)).isEmpty();
	}
}
