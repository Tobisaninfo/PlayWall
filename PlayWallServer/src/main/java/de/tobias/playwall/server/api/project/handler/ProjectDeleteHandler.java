package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectDeleteRequest;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
@RequestHandlerTyped(ProjectDeleteRequest.class)
class ProjectDeleteHandler implements OneTimeActionRequestHandler<ProjectDeleteRequest>
{
	private final ProjectService projectService;

	@Override
	public void handleRequest(ProjectDeleteRequest requestMessage) throws IOException
	{
		final boolean success = projectService.deleteProjectById(requestMessage.getProjectId());
		if(success)
		{
			return;
		}

		throw new ProjectNotExistsException(requestMessage.getProjectId());
	}
}
