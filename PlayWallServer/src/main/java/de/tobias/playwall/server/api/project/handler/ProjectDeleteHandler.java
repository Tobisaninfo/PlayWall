package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectDeleteRequest;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectDeleteRequest.class)
class ProjectDeleteHandler implements RequestHandler<ProjectDeleteRequest>
{
	private final ProjectService projectService;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectDeleteRequest requestMessage) throws IOException, PlayWallServerException
	{
		final boolean success = projectService.deleteProjectById(requestMessage.getProjectId());
		if(success)
		{
			return Optional.empty();
		}

		final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
		throw new PlayWallServerException(messageSource, error);
	}
}
