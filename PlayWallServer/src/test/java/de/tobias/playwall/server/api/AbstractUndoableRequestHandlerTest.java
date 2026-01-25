package de.tobias.playwall.server.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerFactory;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class AbstractUndoableRequestHandlerTest<T extends RequestMessage>
{
	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected ProjectController projectController;

	@Autowired
	protected RequestHandlerFactory requestHandlerFactory;

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void testInverseOperation(UndoableRequestHandler<T> handler, T requestMessage) throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();

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
