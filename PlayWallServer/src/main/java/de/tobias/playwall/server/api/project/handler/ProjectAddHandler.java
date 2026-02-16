package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectAddRequest;
import de.tobias.playwall.common.api.project.request.ProjectAddResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.ProjectMetadataMapper;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectAddRequest.class)
class ProjectAddHandler implements GetRequestHandler<ProjectAddRequest> // TODO: OneTimeAction with update listener?
{
	private final ProjectService projectService;
	private final ProjectMetadataMapper mapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectAddRequest requestMessage) throws IOException
	{
		final ProjectMetadata projectMetadata = projectService.addProject(requestMessage.getName(), requestMessage.getNumberOfHorizontalPads(), requestMessage.getNumberOVerticalPads());
		return Optional.of(new ProjectAddResponse(requestMessage.getMessageId(), mapper.projectMetadataToProjectMetadataDto(projectMetadata)));
	}
}
