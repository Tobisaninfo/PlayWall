package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.common.api.project.ProjectAddRequest;
import de.tobias.playwall.common.api.project.ProjectAddResponse;
import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectMapper;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.api.project.model.Project;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectAddRequest.class)
public class ProjectAddHandler implements RequestHandler<ProjectAddRequest>
{
	private final ProjectRepository projectRepository;
	private final ProjectMapper mapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectAddRequest requestMessage) throws IOException, PlayWallServerException
	{
		final Optional<Project> projectOptional = projectRepository.addProject(requestMessage.getName(), requestMessage.getNumberOfHorizontalPads(), requestMessage.getNumberOVerticalPads());
		if(projectOptional.isPresent())
		{
			return Optional.of(new ProjectAddResponse(requestMessage.getMessageId(), mapper.projectToProjectMetadataDto(projectOptional.get())));
		}
		else
		{
			final ProjectNameAlreadyExistsError error = new ProjectNameAlreadyExistsError(requestMessage.getName());
			throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getName()), error);
		}
	}
}
