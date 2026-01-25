package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.api.project.request.ProjectLoadRequest;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;

import java.io.IOException;

@AllArgsConstructor
@RequestHandlerTyped(ProjectLoadRequest.class)
class ProjectLoadHandler implements OneTimeActionRequestHandler<ProjectLoadRequest>
{
	private final ProjectService projectService;
	private final ProjectController projectController;
	private final MessageSource messageSource;

	@Override
	public void handleRequest(ProjectLoadRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Project project = projectService.getProjectById(requestMessage.getProjectId());
			projectController.loadProject(project);
		}
		catch(ProjectNotExistsException _)
		{
			final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
			throw new PlayWallServerException(messageSource, error);
		}
	}
}