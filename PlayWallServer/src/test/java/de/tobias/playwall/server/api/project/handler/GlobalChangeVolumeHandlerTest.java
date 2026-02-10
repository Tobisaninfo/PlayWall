package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.pad.update.PadUpdate;
import de.tobias.playwall.common.api.project.request.GlobaleChangeVolumeRequest;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.config.SyncAsyncConfig;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Paths;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
@Import(SyncAsyncConfig.class)
@ExtendWith(MockitoExtension.class)
class GlobalChangeVolumeHandlerTest
{
	@Autowired
	private ApplicationEvents applicationEvents;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Mock
	private AudioHandler audioHandler;

	@Autowired
	private JsonMapper objectMapper;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private GlobalChangeVolumeHandler handler;

	@BeforeEach
	void init()
	{
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);
	}

	@Test
	void testGlobalChangeVolumeHandler() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_2.mp3")).toURI()).toAbsolutePath().toString();
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).volume(0.25).build());
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final GlobaleChangeVolumeRequest request = new GlobaleChangeVolumeRequest(0.5);
		handler.handleRequest(request);

		assertThat(project.getPad(padId).getContent()).isInstanceOf(AudioPadContent.class)
				.satisfies(padContent -> assertThat(((AudioPadContent) padContent).getVolume()).isEqualTo(0.25));
		verify(audioHandler).setVolume(0.125);
		assertThat(applicationEvents.stream(PadUpdate.class)).isEmpty();
	}
}
