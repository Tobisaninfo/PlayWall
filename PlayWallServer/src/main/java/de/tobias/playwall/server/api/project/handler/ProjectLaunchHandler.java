package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.common.api.project.ProjectLaunchRequest;
import de.tobias.playwall.common.api.project.ProjectLaunchResponse;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectMetadataRepository;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.model.Project;
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
	private final ProjectMetadataRepository projectMetadataRepository;
	private final ProjectController projectController;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectLaunchRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Project project = projectMetadataRepository.getProjectById(requestMessage.getProjectId());
			projectController.loadProject(project);
		}
		catch(ProjectNotExistsException e)
		{
			final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
			throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getProjectId()), error);
		}
		return Optional.of(new ProjectLaunchResponse(requestMessage.getMessageId()));
	}
}