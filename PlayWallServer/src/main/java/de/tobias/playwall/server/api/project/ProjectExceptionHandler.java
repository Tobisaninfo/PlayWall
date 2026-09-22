package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.net.ErrorMessage;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.net.exception.WsExceptionAdvice;
import de.tobias.playwall.server.net.exception.WsExceptionHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@WsExceptionAdvice
@AllArgsConstructor
public class ProjectExceptionHandler
{
	private final MessageSource messageSource;

	@WsExceptionHandler(ProjectNotExistsException.class)
	ErrorMessage handleProjectNotExistsException(RequestMessage requestMessage, ProjectNotExistsException e)
	{
		final ProjectNotExistsError error = new ProjectNotExistsError(e.getProjectId());
		return new ErrorMessage(requestMessage.getMessageId(), messageSource.getMessage(error.getLocalizationKey(), error.getMessageArguments(), LocaleContextHolder.getLocale()), error);
	}

	@WsExceptionHandler(ProjectNameAlreadyExistsException.class)
	ErrorMessage handleProjectNameAlreadyExistsException(RequestMessage requestMessage, ProjectNameAlreadyExistsException e)
	{
		final ProjectNameAlreadyExistsError error = new ProjectNameAlreadyExistsError(e.getName());
		return new ErrorMessage(requestMessage.getMessageId(), messageSource.getMessage(error.getLocalizationKey(), error.getMessageArguments(), LocaleContextHolder.getLocale()), error);
	}

	@WsExceptionHandler(ProjectNotLoadedException.class)
	ErrorMessage handleProjectNotLoadedException(RequestMessage requestMessage, ProjectNotLoadedException e)
	{
		final ProjectNotLoadedError error = new ProjectNotLoadedError();
		return new ErrorMessage(requestMessage.getMessageId(), messageSource.getMessage(error.getLocalizationKey(), error.getMessageArguments(), LocaleContextHolder.getLocale()), error);
	}
}
