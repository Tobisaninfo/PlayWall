package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.common.api.project.ProjectAddRequest;
import de.tobias.playwall.common.api.project.ProjectAddResponse;
import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectMetadataMapper;
import de.tobias.playwall.server.api.project.ProjectNameAlreadyExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectAddRequest.class)
public class ProjectAddHandler implements RequestHandler<ProjectAddRequest>
{
	private final ProjectService projectService;
	private final ProjectMetadataMapper mapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectAddRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final ProjectMetadata projectMetadata = projectService.addProject(requestMessage.getName(), requestMessage.getNumberOfHorizontalPads(), requestMessage.getNumberOVerticalPads());
			return Optional.of(new ProjectAddResponse(requestMessage.getMessageId(), mapper.projectMetadataToProjectMetadataDto(projectMetadata)));
		}
		catch(ProjectNameAlreadyExistsException e)
		{
			final ProjectNameAlreadyExistsError error = new ProjectNameAlreadyExistsError(requestMessage.getName());
			throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getName()), error);
		}
	}
}
