package de.tobias.playwall.server.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.pad.update.PadPlayPositionUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@RecordApplicationEvents
class PlaybackPositionWatcherTest
{
	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private PlaybackPositionWatcher watcher;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectController projectController;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	final UUID padId1 = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
	final UUID padId2 = UUID.fromString("efb30a6f-593b-4a15-94db-faa2d4117e4f");

	@BeforeEach
	void init() throws Exception
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandler.getPosition()).thenReturn(Duration.ofMillis(5000L));
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_3.json");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();

		project.getPad(padId1).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());
		project.getPad(padId2).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());

		projectController.loadProject(project).get();
	}

	@Test
	void testRunNoPadsPlays()
	{
		watcher.run();
		assertThat(applicationEvents.stream(PadPlayPositionUpdate.class)).isEmpty();
	}

	@Test
	void testRunOnePadPlay()
	{
		projectController.getPadController(padId1).setStatus(PadControllerStatus.PLAY);

		watcher.run();
		assertThat(applicationEvents.stream(PadPlayPositionUpdate.class)).hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getPositions()).hasSize(1).first()
						.satisfies(position -> assertThat(position.padId()).isEqualTo(padId1))
						.satisfies(position -> assertThat(position.millis()).isEqualTo(5000L))
				);
	}

	@Test
	void testRunOnePadPaused()
	{
		projectController.getPadController(padId1).setStatus(PadControllerStatus.PAUSE);

		watcher.run();
		assertThat(applicationEvents.stream(PadPlayPositionUpdate.class)).isEmpty();
	}

	@Test
	void testRunTwoPadsPlay()
	{
		projectController.getPadController(padId1).setStatus(PadControllerStatus.PLAY);
		projectController.getPadController(padId2).setStatus(PadControllerStatus.PLAY);

		watcher.run();
		assertThat(applicationEvents.stream(PadPlayPositionUpdate.class)).hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getPositions()).hasSize(2));
	}

}
