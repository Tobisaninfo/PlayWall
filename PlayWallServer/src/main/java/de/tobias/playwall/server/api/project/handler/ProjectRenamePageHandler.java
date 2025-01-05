package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.common.api.project.PageNotExistsError;
import de.tobias.playwall.common.api.project.ProjectAddPageResponse;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.api.project.ProjectRenamePageRequest;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.PageMapper;
import de.tobias.playwall.server.api.project.PageNotExistsException;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Page;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectRenamePageRequest.class)
public class ProjectRenamePageHandler implements RequestHandler<ProjectRenamePageRequest>
{
	private final ProjectService projectService;
	private final PageMapper mapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectRenamePageRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Page page = projectService.renamePage(requestMessage.getProjectId(), requestMessage.getPageId(), requestMessage.getNewName());
			return Optional.of(new ProjectAddPageResponse(requestMessage.getMessageId(), mapper.pageToPageDto(page)));
		}
		catch(ProjectNotExistsException e)
		{
			final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
			throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getProjectId()), error);
		}
		catch(PageNotExistsException e)
		{
			final PageNotExistsError error = new PageNotExistsError(requestMessage.getProjectId(), requestMessage.getPageId());
			throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getProjectId()), error);
		}
	}
}
