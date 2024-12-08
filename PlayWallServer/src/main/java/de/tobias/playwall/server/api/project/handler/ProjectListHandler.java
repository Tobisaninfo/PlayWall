package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectListRequest;
import de.tobias.playwall.common.api.project.ProjectListResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.ProjectMetadataMapper;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(ProjectListRequest.class)
@AllArgsConstructor
public class ProjectListHandler implements RequestHandler<ProjectListRequest>
{
	private final ProjectService projectService;
	private final ProjectMetadataMapper mapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectListRequest requestMessage) throws IOException
	{
		return Optional.of(new ProjectListResponse(requestMessage.getMessageId(), projectService.getAllProjectMetadata()
				.stream()
				.map(mapper::projectMetadataToProjectMetadataDto)
				.toList()));
	}
}
