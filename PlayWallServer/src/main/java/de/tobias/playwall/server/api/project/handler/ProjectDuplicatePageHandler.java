package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.common.api.project.PageNotExistsError;
import de.tobias.playwall.common.api.project.ProjectAddPageResponse;
import de.tobias.playwall.common.api.project.ProjectDuplicatePageRequest;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.*;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectDuplicatePageRequest.class)
public class ProjectDuplicatePageHandler implements RequestHandler<ProjectDuplicatePageRequest>
{
	private final ProjectRepository projectRepository;
	private final PageMetadataMapper mapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectDuplicatePageRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Page page = projectRepository.duplicatePage(requestMessage.getProjectId(), requestMessage.getPageId(), requestMessage.getName());
			return Optional.of(new ProjectAddPageResponse(requestMessage.getMessageId(), mapper.pageMetadataToPageMetadataDto(page)));
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
