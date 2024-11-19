package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectAddRequest;
import de.tobias.playwall.common.api.project.ProjectAddResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.ProjectMetadata;
import de.tobias.playwall.server.api.project.ProjectMetadataMapper;
import de.tobias.playwall.server.api.project.ProjectRepository;
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
	private final ProjectMetadataMapper mapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectAddRequest requestMessage) throws IOException
	{
		final Optional<ProjectMetadata> projectOptional = projectRepository.addProject(requestMessage.getName());
		if(projectOptional.isPresent())
		{
			return Optional.of(new ProjectAddResponse(requestMessage.getMessageId(), true, mapper.projectMetadataToProjectMetadataDto(projectOptional.get())));
		}
		else
		{
			return Optional.of(new ProjectAddResponse(requestMessage.getMessageId(), false, null));
		}
	}
}
