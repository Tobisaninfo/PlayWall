package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectListRequest;
import de.tobias.playwall.common.api.project.ProjectListResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.ProjectMapper;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.util.Optional;

@RequestHandlerTyped(ProjectListRequest.class)
@AllArgsConstructor
public class ProjectListHandler implements RequestHandler<ProjectListRequest>
{
	private final ProjectRepository repository;
	private final ProjectMapper mapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectListRequest requestMessage)
	{
		return Optional.of(new ProjectListResponse(requestMessage.getMessageId(), repository.getAllProjectMetadata()
				.stream()
				.map(mapper::projectToProjectMetadataDto)
				.toList()));
	}
}
