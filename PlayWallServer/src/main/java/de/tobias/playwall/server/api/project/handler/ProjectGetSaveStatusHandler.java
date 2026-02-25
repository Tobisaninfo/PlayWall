package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectGetSaveStatusRequest;
import de.tobias.playwall.common.api.project.request.ProjectGetSaveStatusResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectGetSaveStatusRequest.class)
class ProjectGetSaveStatusHandler implements GetRequestHandler<ProjectGetSaveStatusRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectGetSaveStatusRequest requestMessage) throws IOException
	{
		final Project loadedProject = projectController.getLoadedProject();
		final Project projectFromFile = projectService.getProjectById(loadedProject.getMetadata().getId());

		boolean isSaved = loadedProject.equals(projectFromFile);

		return Optional.of(new ProjectGetSaveStatusResponse(requestMessage.getMessageId(), isSaved));
	}
}