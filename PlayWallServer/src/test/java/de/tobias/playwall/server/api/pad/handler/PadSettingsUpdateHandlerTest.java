package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.common.TimeMode;
import de.tobias.playwall.common.api.pad.AudioPadContentDto;
import de.tobias.playwall.common.api.pad.PadDto;
import de.tobias.playwall.common.api.pad.request.PadNotExistsError;
import de.tobias.playwall.common.api.pad.request.PadSettingsUpdateRequest;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.config.SyncAsyncConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

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
class PadSettingsUpdateHandlerTest extends AbstractUndoableRequestHandlerTest<PadSettingsUpdateRequest>
{
	@Autowired
	private ApplicationEvents applicationEvents;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private PadSettingsUpdateHandler handler;

	@BeforeEach
	void init()
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);
	}

	@Test
	void testPadSettingsUpdateHandler() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final String oldMediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_2.mp3")).toURI()).toAbsolutePath().toString();
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(oldMediaPath).loop(true).build());
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final PadSettingsUpdateRequest request = new PadSettingsUpdateRequest(padId, PadDto.builder()
				.name("Lorem")
				.timeMode(TimeMode.ELAPSED_AND_TOTAL)
				.defaultColor(Color.BLUE1)
				.playColor(Color.LIGHT_BLUE2)
				.content(AudioPadContentDto.builder().mediaPath(oldMediaPath).loop(false).build())
				.build());
		handler.handleRequest(request);

		assertThat(project.getPad(padId).getName()).isEqualTo("Lorem");
		assertThat(project.getPad(padId).getContent()).isInstanceOf(AudioPadContent.class);

		assertThat(applicationEvents.stream(PadUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getPad().getName()).isEqualTo("Lorem"))
				.satisfies(event -> assertThat(event.getPad().getTimeMode()).isEqualTo(TimeMode.ELAPSED_AND_TOTAL))
				.satisfies(event -> assertThat(event.getPad().getDefaultColor()).isEqualTo(Color.BLUE1))
				.satisfies(event -> assertThat(event.getPad().getPlayColor()).isEqualTo(Color.LIGHT_BLUE2))
				.satisfies(event -> assertThat(((AudioPadContentDto) event.getPad().getContent()).isLoop()).isFalse());
	}

	@Test
	void testPadSettingsUpdateHandlerPadNotFound() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2d55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final PadSettingsUpdateRequest request = new PadSettingsUpdateRequest(padId, PadDto.builder().name("Lorem").build());
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(PlayWallServerException.class)
				.extracting(e -> ((PlayWallServerException) e).getError())
				.isInstanceOf(PadNotExistsError.class);

		assertThat(applicationEvents.stream(PadUpdate.class)).isEmpty();
	}

	@Test
	void testUndoOperation() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());

		final PadSettingsUpdateRequest request = new PadSettingsUpdateRequest(padId, PadDto.builder()
				.name("Lorem")
				.content(AudioPadContentDto.builder().mediaPath("abc.mp3").loop(true).build())
				.build());

		testInverseOperation(project, handler, request);
	}
}
