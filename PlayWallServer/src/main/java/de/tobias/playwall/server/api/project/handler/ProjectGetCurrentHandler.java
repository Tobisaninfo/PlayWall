package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectGetCurrentRequest;
import de.tobias.playwall.common.api.project.request.ProjectGetCurrentResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.ProjectMapper;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectGetCurrentRequest.class)
class ProjectGetCurrentHandler implements GetRequestHandler<ProjectGetCurrentRequest>
{
	private final ProjectMapper projectMapper;
	private final ProjectController projectController;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectGetCurrentRequest requestMessage) throws IOException
	{
		final Project project = projectController.getLoadedProject();
		return Optional.of(new ProjectGetCurrentResponse(
				requestMessage.getMessageId(),
				projectMapper.projectToProjectDto(project),
				projectController.getCurrentPageIndex(),
				projectController.getAllPadStatusById())
		);
	}
}
