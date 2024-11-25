package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectAddRequest;
import de.tobias.playwall.common.api.project.ProjectAddResponse;
import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.Project;
import de.tobias.playwall.server.api.project.ProjectMapper;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.text.MessageFormat;
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
		final Optional<Project> projectOptional = projectRepository.addProject(requestMessage.getName());
		if(projectOptional.isPresent())
		{
			return Optional.of(new ProjectAddResponse(requestMessage.getMessageId(), true, mapper.projectToProjectMetadataDto(projectOptional.get())));
		}
		else
		{
			final ProjectNameAlreadyExistsError error = new ProjectNameAlreadyExistsError(requestMessage.getName());
			throw new PlayWallServerException(MessageFormat.format("Das Projekt mit dem Namen \"{0}\" kann nicht angelegt werden, da bereits ein Projekt mit diesem Namen existiert.", requestMessage.getName()), error);
		}
	}
}
