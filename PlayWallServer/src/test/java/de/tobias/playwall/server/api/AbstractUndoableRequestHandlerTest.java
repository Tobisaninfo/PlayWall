package de.tobias.playwall.server.api;

import de.tobias.playwall.common.api.history.RedoRequest;
import de.tobias.playwall.common.api.history.UndoRequest;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestExecutor;
import de.tobias.playwall.server.net.RequestHandlerFactory;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class AbstractUndoableRequestHandlerTest<T extends RequestMessage> extends AbstractRequestHandlerTest
{
	@Autowired
	protected JsonMapper objectMapper;

	@Autowired
	private RequestExecutor requestExecutor;

	@Autowired
	@MockitoSpyBean
	protected ProjectController projectController;

	@Autowired
	protected RequestHandlerFactory requestHandlerFactory;

	@SuppressWarnings({"unchecked", "rawtypes", "java:S1871"})
	protected void testInverseOperation(Project project, T requestMessage) throws Exception
	{
		projectController.loadProject(project).get();
		final Project expected = project.copy(false);

		// Execute request
		requestExecutor.execute(requestMessage);
		assertThat(project).isNotEqualTo(expected);

		// Undo Request
		final OneTimeActionRequestHandler undoHandler = (OneTimeActionRequestHandler) requestHandlerFactory.getRequestHandler(UndoRequest.class).orElseThrow();
		undoHandler.handleRequest(new UndoRequest());

		assertThat(project).isEqualTo(expected);

		// Redo Request
		final OneTimeActionRequestHandler redoHandler = (OneTimeActionRequestHandler) requestHandlerFactory.getRequestHandler(RedoRequest.class).orElseThrow();
		redoHandler.handleRequest(new RedoRequest());

		assertThat(project).isNotEqualTo(expected);

		// Undo Again
		undoHandler.handleRequest(new UndoRequest());
		assertThat(project).isEqualTo(expected);
	}
}
