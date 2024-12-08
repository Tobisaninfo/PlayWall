package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.common.api.project.ProjectDeleteRequest;
import de.tobias.playwall.common.api.project.ProjectDeleteResponse;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectDeleteRequest.class)
public class ProjectDeleteHandler implements RequestHandler<ProjectDeleteRequest>
{
	private final ProjectService projectService;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectDeleteRequest requestMessage) throws IOException, PlayWallServerException
	{
		final boolean success = projectService.deleteProjectById(requestMessage.getProjectId());
		if(success)
		{
			return Optional.of(new ProjectDeleteResponse(requestMessage.getMessageId()));
		}

		final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
		throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getProjectId()), error);
	}
}
