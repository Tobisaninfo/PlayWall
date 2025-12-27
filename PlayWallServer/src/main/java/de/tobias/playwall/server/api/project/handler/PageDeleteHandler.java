package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.PageNotExistsError;
import de.tobias.playwall.common.api.project.PageDeleteRequest;
import de.tobias.playwall.common.api.project.PageDeleteResponse;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(PageDeleteRequest.class)
public class PageDeleteHandler implements RequestHandler<PageDeleteRequest>
{
	private final ProjectService projectService;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(PageDeleteRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final boolean success = projectService.deletePage(requestMessage.getProjectId(), requestMessage.getPageId());
			if(success)
			{
				return Optional.of(new PageDeleteResponse(requestMessage.getMessageId()));
			}

			final PageNotExistsError error = new PageNotExistsError(requestMessage.getProjectId(), requestMessage.getPageId());
			throw new PlayWallServerException(messageSource.getMessage(error.getLocalizationKey(), new Object[]{requestMessage.getProjectId()}, LocaleContextHolder.getLocale()), error);
		}
		catch(ProjectNotExistsException _)
		{
			final ProjectNotExistsError error = new ProjectNotExistsError(requestMessage.getProjectId());
			throw new PlayWallServerException(messageSource.getMessage(error.getLocalizationKey(), new Object[]{requestMessage.getProjectId()}, LocaleContextHolder.getLocale()), error);
		}
	}
}
