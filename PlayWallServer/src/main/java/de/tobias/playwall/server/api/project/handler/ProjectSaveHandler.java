package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.api.project.ProjectSaveRequest;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectMetadataRepository;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(ProjectSaveRequest.class)
@AllArgsConstructor
class ProjectSaveHandler implements RequestHandler<ProjectSaveRequest>
{
	private final ProjectController projectController;
	private final ProjectRepository projectRepository;
	private final ProjectMetadataRepository projectMetadataRepository;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectSaveRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Project loadedProject = projectController.getLoadedProject();
			projectRepository.saveProject(loadedProject);
			projectMetadataRepository.saveProjects();
			return Optional.empty();
		}
		catch(ProjectNotLoadedException _)
		{
			final ProjectNotLoadedError error = new ProjectNotLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}
	}
}
