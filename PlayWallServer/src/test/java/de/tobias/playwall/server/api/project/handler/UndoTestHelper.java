package de.tobias.playwall.server.api.project.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.RequestHandlerFactory;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.test.context.event.ApplicationEvents;

import java.nio.file.Paths;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

public class UndoTestHelper
{
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void testInverseOperation(ObjectMapper objectMapper, ProjectController projectController, ApplicationEvents applicationEvents, UndoableRequestHandler handler, RequestMessage requestMessage, RequestHandlerFactory requestHandlerFactory) throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final String mediaPath = Paths.get(requireNonNull(UndoTestHelper.class.getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();

		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());
		projectController.loadProject(project).get();
		final Project expected = project.copy();

		final UndoItem undoItem = handler.getInverseOperation(requestMessage);

		handler.handleRequest(requestMessage);
		assertThat(project).isNotEqualTo(expected);

		final RequestHandler inverseHandler = requestHandlerFactory.getRequestHandler(undoItem.inverseRequest().getClass()).orElseThrow();
		inverseHandler.handleRequest(undoItem.inverseRequest());

		assertThat(project).isEqualTo(expected);
	}
}
