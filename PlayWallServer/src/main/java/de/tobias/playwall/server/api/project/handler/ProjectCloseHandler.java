package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectCloseRequest;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
@RequestHandlerTyped(ProjectCloseRequest.class)
class ProjectCloseHandler implements OneTimeActionRequestHandler<ProjectCloseRequest>
{
	private final ProjectController projectController;

	@Override
	public void handleRequest(ProjectCloseRequest requestMessage) throws IOException
	{
		projectController.unloadProject();
	}
}