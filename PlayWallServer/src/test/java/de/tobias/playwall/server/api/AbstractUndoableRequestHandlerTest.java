package de.tobias.playwall.server.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.*;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class AbstractUndoableRequestHandlerTest<T extends RequestMessage>
{
	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected ProjectController projectController;

	@Autowired
	protected RequestHandlerFactory requestHandlerFactory;

	@SuppressWarnings({"unchecked", "rawtypes", "java:S1871"})
	protected void testInverseOperation(Project project, UndoableRequestHandler<T> handler, T requestMessage) throws Exception
	{
		projectController.loadProject(project).get();
		final Project expected = project.copy();


		final Optional<UndoItem> undoItemOptional = handler.handleRequest(requestMessage);
		assertThat(project).isNotEqualTo(expected);


		if(undoItemOptional.isEmpty())
		{
			fail("No undo item was created.");
		}

		final UndoItem undoItem = undoItemOptional.get();
		final RequestHandler inverseHandler = requestHandlerFactory.getRequestHandler(undoItem.inverseRequest().getClass()).orElseThrow();

		switch(inverseHandler)
		{
			case UndoableRequestHandler handler2 -> handler2.handleRequest(undoItem.inverseRequest());
			case OneTimeActionRequestHandler handler2 -> handler2.handleRequest(undoItem.inverseRequest());
			case GetRequestHandler handler2 -> handler2.handleRequest(undoItem.inverseRequest());
		}

		assertThat(project).isEqualTo(expected);
	}
}
