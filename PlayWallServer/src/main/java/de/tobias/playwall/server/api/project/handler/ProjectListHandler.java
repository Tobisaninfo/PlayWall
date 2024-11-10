package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.net.project.ProjectListRequest;
import de.tobias.playwall.common.net.project.ProjectListResponse;
import de.tobias.playwall.server.api.project.ProjectMetadataMapper;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(ProjectListRequest.class)
@AllArgsConstructor
public class ProjectListHandler implements RequestHandler<ProjectListRequest>
{
	private final ProjectRepository repository;
	private final ProjectMetadataMapper mapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectListRequest requestMessage) throws IOException
	{
		return Optional.of(new ProjectListResponse(requestMessage.getMessageId(), repository.getAllProjectMetadata()
				.stream()
				.map(mapper::projectMetadataToProjectMapperDto)
				.toList()));
	}
}
