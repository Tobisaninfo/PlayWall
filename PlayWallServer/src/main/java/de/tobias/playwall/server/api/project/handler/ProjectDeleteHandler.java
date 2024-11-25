package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectDeleteRequest;
import de.tobias.playwall.common.api.project.ProjectDeleteResponse;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectDeleteRequest.class)
public class ProjectDeleteHandler implements RequestHandler<ProjectDeleteRequest>
{
	private final ProjectRepository projectRepository;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectDeleteRequest requestMessage) throws IOException, PlayWallServerException
	{
		final boolean success = projectRepository.deleteProject(requestMessage.getProjectId());
		if(success)
		{
			return Optional.of(new ProjectDeleteResponse(requestMessage.getMessageId()));
		}

		final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
		throw new PlayWallServerException(MessageFormat.format("Es existiert kein Projekt mit der ID \"{0}\".", requestMessage.getProjectId()), error);
	}
}
