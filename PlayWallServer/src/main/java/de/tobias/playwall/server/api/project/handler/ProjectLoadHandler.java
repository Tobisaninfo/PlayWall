package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectLoadRequest;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
@RequestHandlerTyped(ProjectLoadRequest.class)
class ProjectLoadHandler implements OneTimeActionRequestHandler<ProjectLoadRequest>
{
	private final ProjectService projectService;
	private final ProjectController projectController;

	@Override
	public void handleRequest(ProjectLoadRequest requestMessage) throws IOException
	{
		final Project project = projectService.getProjectById(requestMessage.getProjectId());
		projectController.loadProject(project);
		projectService.onProjectOpened(requestMessage.getProjectId());
	}
}