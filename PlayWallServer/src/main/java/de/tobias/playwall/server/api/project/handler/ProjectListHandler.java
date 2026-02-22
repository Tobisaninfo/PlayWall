package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectListRequest;
import de.tobias.playwall.common.api.project.request.ProjectListResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.AllProjectsInfoMapper;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(ProjectListRequest.class)
@AllArgsConstructor
class ProjectListHandler implements GetRequestHandler<ProjectListRequest>
{
	private final ProjectService projectService;
	private final AllProjectsInfoMapper allProjectsInfoMapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectListRequest requestMessage) throws IOException
	{
		return Optional.of(new ProjectListResponse(requestMessage.getMessageId(), allProjectsInfoMapper.allProjectsInfoToAllProjectsInfoDto(projectService.getAllProjectsInfo())));
	}
}
