package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectDuplicateRequest;
import de.tobias.playwall.common.api.project.request.ProjectDuplicateResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@RequestHandlerTyped(ProjectDuplicateRequest.class)
class ProjectDuplicateHandler implements GetRequestHandler<ProjectDuplicateRequest>
{
	private final ProjectService projectService;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectDuplicateRequest requestMessage) throws IOException
	{
		final UUID projectId = projectService.duplicateProject(requestMessage.getProjectId());
		return Optional.of(new ProjectDuplicateResponse(requestMessage.getMessageId(), projectId));
	}
}