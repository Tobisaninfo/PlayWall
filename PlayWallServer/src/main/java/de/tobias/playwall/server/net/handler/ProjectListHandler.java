package de.tobias.playwall.server.net.handler;

import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.net.project.ProjectListRequest;
import de.tobias.playwall.common.net.project.ProjectListResponse;
import de.tobias.playwall.common.net.project.ProjectMetadata;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequestHandlerTyped(ProjectListRequest.class)
public class ProjectListHandler implements RequestHandler<ProjectListRequest>
{
	@Override
	public Optional<ResponseMessage> handleRequest(ProjectListRequest requestMessage)
	{
		return Optional.of(new ProjectListResponse(requestMessage.getMessageId(), List.of(
				new ProjectMetadata("abc", LocalDateTime.now()),
				new ProjectMetadata("def", LocalDateTime.now())
		)));
	}
}
