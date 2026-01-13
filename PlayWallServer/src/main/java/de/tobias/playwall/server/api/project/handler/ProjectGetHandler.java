package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectGetRequest;
import de.tobias.playwall.common.api.project.ProjectGetResponse;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectMapper;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectGetRequest.class)
public class ProjectGetHandler implements RequestHandler<ProjectGetRequest>
{
	private final ProjectService projectService;
	private final ProjectMapper projectMapper;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectGetRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Project project = projectService.getProjectById(requestMessage.getProjectId());
			return Optional.of(new ProjectGetResponse(requestMessage.getMessageId(), projectMapper.projectToProjectDto(project)));
		}
		catch(ProjectNotExistsException _)
		{
			final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
			throw new PlayWallServerException(messageSource, error);
		}
	}
}