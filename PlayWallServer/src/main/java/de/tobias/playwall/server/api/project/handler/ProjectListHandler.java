package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.model.AllProjectsInfoDto;
import de.tobias.playwall.common.api.project.request.ProjectListRequest;
import de.tobias.playwall.common.api.project.request.ProjectListResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectMetadataMapper;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(ProjectListRequest.class)
@AllArgsConstructor
class ProjectListHandler implements GetRequestHandler<ProjectListRequest>
{
	private final AllProjectsInfoRepository allProjectsInfoRepository;
	private final ProjectMetadataMapper projectMetadataMapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectListRequest requestMessage) throws IOException
	{
		return Optional.of(new ProjectListResponse(requestMessage.getMessageId(), new AllProjectsInfoDto(
				allProjectsInfoRepository.getRecentProjectIds(),
				projectMetadataMapper.projectMetadataToProjectMetadataDto(allProjectsInfoRepository.getAllProjectMetadata())
		)));
	}
}
