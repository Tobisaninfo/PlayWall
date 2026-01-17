package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectLoadRequest;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectLoadRequest.class)
class ProjectLoadHandler implements RequestHandler<ProjectLoadRequest>
{
	private final ProjectService projectService;
	private final ProjectController projectController;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectLoadRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Project project = projectService.getProjectById(requestMessage.getProjectId());
			projectController.loadProject(project);
			return Optional.empty();
		}
		catch(ProjectNotExistsException _)
		{
			final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
			throw new PlayWallServerException(messageSource, error);
		}
	}
}