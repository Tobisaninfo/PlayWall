package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.api.project.request.ProjectDeleteRequest;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;

import java.io.IOException;

@AllArgsConstructor
@RequestHandlerTyped(ProjectDeleteRequest.class)
class ProjectDeleteHandler implements OneTimeActionRequestHandler<ProjectDeleteRequest>
{
	private final ProjectService projectService;
	private final MessageSource messageSource;

	@Override
	public void handleRequest(ProjectDeleteRequest requestMessage) throws IOException, PlayWallServerException
	{
		final boolean success = projectService.deleteProjectById(requestMessage.getProjectId());
		if(success)
		{
			return;
		}

		final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
		throw new PlayWallServerException(messageSource, error);
	}
}
