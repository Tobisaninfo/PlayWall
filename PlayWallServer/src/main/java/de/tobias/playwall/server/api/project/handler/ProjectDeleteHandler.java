package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.api.project.ProjectDeleteRequest;
import de.tobias.playwall.common.api.project.ProjectDeleteResponse;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectDeleteRequest.class)
public class ProjectDeleteHandler implements RequestHandler<ProjectDeleteRequest>
{
	private final ProjectRepository projectRepository;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectDeleteRequest requestMessage)
	{
		final boolean success = projectRepository.deleteProject(requestMessage.getProjectId());
		return Optional.of(new ProjectDeleteResponse(requestMessage.getMessageId(), success));
	}
}
