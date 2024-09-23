package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.net.project.ProjectDeleteRequest;
import de.tobias.playwall.common.net.project.ProjectDeleteResponse;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;

import java.util.Optional;

@RequestHandlerTyped(ProjectDeleteRequest.class)
public class ProjectDeleteHandler implements RequestHandler<ProjectDeleteRequest>
{
	@Override
	public Optional<ResponseMessage> handleRequest(ProjectDeleteRequest requestMessage)
	{
		return Optional.of(new ProjectDeleteResponse(requestMessage.getMessageId(), true));
	}
}
