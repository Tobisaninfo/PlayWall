package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.NoProjectLoadedError;
import de.tobias.playwall.common.api.project.ProjectSaveRequest;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
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
public class ProjectSaveHandler implements RequestHandler<ProjectSaveRequest>
{
	private final ProjectController projectController;
	private final ProjectRepository projectRepository;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectSaveRequest requestMessage) throws IOException, PlayWallServerException
	{
		final Project loadedProject = projectController.getLoadedProject();
		if(loadedProject == null)
		{
			final NoProjectLoadedError error = new NoProjectLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}
		projectRepository.saveProject(loadedProject);
		return Optional.empty();
	}
}
