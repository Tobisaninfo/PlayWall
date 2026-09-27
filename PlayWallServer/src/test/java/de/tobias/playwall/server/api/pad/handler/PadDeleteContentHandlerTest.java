package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.pad.request.PadDeleteContentRequest;
import de.tobias.playwall.common.api.pad.update.PadStatusUpdate;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.pad.PadNotExistsException;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.config.SyncAsyncConfig;
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

import java.nio.file.Paths;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
@Import(SyncAsyncConfig.class)
@ExtendWith(MockitoExtension.class)
class PadDeleteContentHandlerTest extends AbstractUndoableRequestHandlerTest<PadDeleteContentRequest>
{
	@Autowired
	private ApplicationEvents applicationEvents;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Mock
	private AudioHandler audioHandler;

	@Autowired
	private PadDeleteContentHandler handler;

	@BeforeEach
	void init()
	{
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
		handler.handleRequest(request);

		assertThat(project.getPad(padId).getContent()).isNull();

		assertThat(applicationEvents.stream(PadUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getPad().getContent()).isNull());
	}

	@Test
	void testPadDeleteMediaHandlerOnAudioPad() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_2.mp3")).toURI()).toAbsolutePath().toString().replace("\\", "/");
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(true).build());
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final PadDeleteContentRequest request = new PadDeleteContentRequest(padId);
		handler.handleRequest(request);

		assertThat(project.getPad(padId).getContent()).isNull();

		verify(audioHandler).stop();
		verify(audioHandler).unloadMedia();
		assertThat(applicationEvents.stream(PadUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getPad().getContent()).isNull());
		assertThat(applicationEvents.stream(PadStatusUpdate.class))
				.last()
				.satisfies(event -> assertThat(event.getStatus()).isEqualTo(PadControllerStatus.EMPTY));
	}

	@Test
	void testPadDeleteMediaHandlerPadNotFound() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2d55-4734-8148-5fb657963616");

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
		applicationEvents.clear();

		final PadDeleteContentRequest request = new PadDeleteContentRequest(padId);

		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(PadNotExistsException.class);

		assertThat(applicationEvents.stream(PadUpdate.class)).isEmpty();
	}

	@Test
	void testUndoOperation() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString().replace("\\", "/");
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());

		final PadDeleteContentRequest request = new PadDeleteContentRequest(padId);

		testInverseOperation(project, request);
	}
}
