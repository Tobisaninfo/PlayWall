package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.api.project.ProjectSettingsUpdateRequest;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectNameAlreadyExistsException;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.MessageSource;

import java.util.Optional;

@RequestHandlerTyped(ProjectSettingsUpdateRequest.class)
public class ProjectSettingsUpdateHandler implements RequestHandler<ProjectSettingsUpdateRequest>
{
	private final ProjectController projectController;

	private final ProjectService projectService;

	private final MessageSource messageSource;

	public ProjectSettingsUpdateHandler(ProjectController projectController, ProjectService projectService, MessageSource messageSource)
	{
		this.projectController = projectController;
		this.projectService = projectService;
		this.messageSource = messageSource;
	}

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectSettingsUpdateRequest requestMessage) throws PlayWallServerException
	{
		final Project project = projectController.getLoadedProject();
		if(project == null)
		{
			final ProjectNotLoadedError error = new ProjectNotLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}

		try
		{
			project.getMetadata().setName(requestMessage.getProjectMetadata().name());
			projectService.rename(project.getMetadata().getId(), requestMessage.getProjectMetadata().name());
		}
		catch(ProjectNameAlreadyExistsException e)
		{
			final ProjectNameAlreadyExistsError error = new ProjectNameAlreadyExistsError(requestMessage.getProjectMetadata().name());
			throw new PlayWallServerException(messageSource, error);
		}
		catch(ProjectNotExistsException e)
		{
			final ProjectNotExistsError error = new ProjectNotExistsError(project.getMetadata().getId());
			throw new PlayWallServerException(messageSource, error);
		}

		return Optional.empty();
	}
}