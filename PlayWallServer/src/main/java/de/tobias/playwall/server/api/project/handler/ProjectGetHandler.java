package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectGetRequest;
import de.tobias.playwall.common.api.project.request.ProjectGetResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.ProjectMapper;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectGetRequest.class)
class ProjectGetHandler implements GetRequestHandler<ProjectGetRequest>
{
	private final ProjectService projectService;
	private final ProjectMapper projectMapper;
	private final ProjectController projectController;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectGetRequest requestMessage) throws IOException
	{
		if(requestMessage.getProjectId() != null) // any project from disk by id
		{
			final Project project = projectService.getProjectById(requestMessage.getProjectId());
			return Optional.of(new ProjectGetResponse(requestMessage.getMessageId(), projectMapper.projectToProjectDto(project)));
		}
		else // Current project is loaded
		{
			final Project project = projectController.getLoadedProject();
			return Optional.of(new ProjectGetResponse(requestMessage.getMessageId(), projectMapper.projectToProjectDto(project), projectController.getCurrentPageIndex()));
		}
	}
}