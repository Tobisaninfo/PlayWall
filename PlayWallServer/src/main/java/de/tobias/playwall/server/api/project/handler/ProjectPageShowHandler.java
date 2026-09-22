package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectPageShowRequest;
import de.tobias.playwall.common.api.project.update.ProjectPageShownUpdate;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@RequestHandlerTyped(ProjectPageShowRequest.class)
@RequiredArgsConstructor
class ProjectPageShowHandler implements OneTimeActionRequestHandler<ProjectPageShowRequest>
{
	private final ProjectController projectController;
	private final ApplicationContext context;

	@Override
	public void handleRequest(ProjectPageShowRequest requestMessage) throws IOException
	{
		// Check if a project is loaded
		projectController.getLoadedProject();

		projectController.setCurrentPageIndex(requestMessage.getIndex());
		context.publishEvent(new ProjectPageShownUpdate(requestMessage.getIndex()));
	}
}
