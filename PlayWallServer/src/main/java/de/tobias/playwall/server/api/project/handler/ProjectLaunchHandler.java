package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.common.api.project.ProjectLaunchRequest;
import de.tobias.playwall.common.api.project.ProjectLaunchResponse;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectMapper;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectLaunchRequest.class)
public class ProjectLaunchHandler implements RequestHandler<ProjectLaunchRequest>
{
	private final ProjectService projectService;
	private final ProjectController projectController;
	private final ProjectMapper projectMapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectLaunchRequest requestMessage) throws IOException, PlayWallServerException
	{
		final Project project;
		try
		{
			project = projectService.getProjectById(requestMessage.getProjectId());
			projectController.loadProject(project);
		}
		catch(ProjectNotExistsException e)
		{
			final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
			throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getProjectId()), error);
		}

		return Optional.of(new ProjectLaunchResponse(requestMessage.getMessageId(), projectMapper.projectToProjectDto(project)));
	}
}