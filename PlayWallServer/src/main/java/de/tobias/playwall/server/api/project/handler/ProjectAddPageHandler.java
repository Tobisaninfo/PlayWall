package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.common.api.project.ProjectAddPageRequest;
import de.tobias.playwall.common.api.project.ProjectAddPageResponse;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.PageMetadataMapper;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.api.project.model.Page;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectAddPageRequest.class)
public class ProjectAddPageHandler implements RequestHandler<ProjectAddPageRequest>
{
	private final ProjectRepository projectRepository;
	private final PageMetadataMapper mapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectAddPageRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Page page = projectRepository.addPage(requestMessage.getProjectId(), requestMessage.getName());
			return Optional.of(new ProjectAddPageResponse(requestMessage.getMessageId(), mapper.pageMetadataToPageMetadataDto(page)));
		}
		catch(ProjectNotExistsException e)
		{
			final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
			throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getProjectId()), error);

		}
	}
}
