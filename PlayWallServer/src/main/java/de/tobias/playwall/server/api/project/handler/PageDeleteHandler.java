package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.PageDeleteRequest;
import de.tobias.playwall.common.api.project.PageNotExistsError;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(PageDeleteRequest.class)
public class PageDeleteHandler implements RequestHandler<PageDeleteRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(PageDeleteRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final boolean success = projectService.deletePage(projectController.getLoadedProject(), requestMessage.getPageId());
			if(success)
			{
				return Optional.empty();
			}

			final PageNotExistsError error = new PageNotExistsError(requestMessage.getPageId());
			throw new PlayWallServerException(messageSource, error);
		}
		catch(ProjectNotLoadedException _)
		{
			final ProjectNotLoadedError error = new ProjectNotLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}
	}
}
