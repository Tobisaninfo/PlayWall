package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectDeleteRequest;
import de.tobias.playwall.common.api.project.ProjectDeleteResponse;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectDeleteRequest.class)
public class ProjectDeleteHandler implements RequestHandler<ProjectDeleteRequest>
{
	private final ProjectService projectService;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectDeleteRequest requestMessage) throws IOException, PlayWallServerException
	{
		final boolean success = projectService.deleteProjectById(requestMessage.getProjectId());
		if(success)
		{
			return Optional.of(new ProjectDeleteResponse(requestMessage.getMessageId()));
		}

		final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
		throw new PlayWallServerException(messageSource.getMessage(error.getLocalizationKey(), new Object[]{requestMessage.getProjectId()}, LocaleContextHolder.getLocale()), error);
	}
}
