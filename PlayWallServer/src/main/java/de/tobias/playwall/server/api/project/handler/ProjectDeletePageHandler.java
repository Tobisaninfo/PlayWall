package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.common.api.project.PageNotExistsError;
import de.tobias.playwall.common.api.project.ProjectDeletePageRequest;
import de.tobias.playwall.common.api.project.ProjectDeletePageResponse;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectDeletePageRequest.class)
public class ProjectDeletePageHandler implements RequestHandler<ProjectDeletePageRequest>
{
	private final ProjectService projectService;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectDeletePageRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final boolean success = projectService.deletePage(requestMessage.getProjectId(), requestMessage.getPageId());
			if(success)
			{
				return Optional.of(new ProjectDeletePageResponse(requestMessage.getMessageId()));
			}

			final PageNotExistsError error = new PageNotExistsError(requestMessage.getProjectId(), requestMessage.getPageId());
			throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getProjectId()), error);
		}
		catch(ProjectNotExistsException e)
		{
			final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
			throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getProjectId()), error);
		}
	}
}
