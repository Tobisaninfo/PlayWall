package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.project.*;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.api.page.PageNotExistsException;
import de.tobias.playwall.server.api.project.*;
import de.tobias.playwall.server.common.model.project.Page;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(PageRenameRequest.class)
public class PageRenameHandler implements RequestHandler<PageRenameRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final PageMapper mapper;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(PageRenameRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Page page = projectService.renamePage(projectController.getLoadedProject(), requestMessage.getPageId(), requestMessage.getNewName());
			return Optional.of(new PageAddResponse(requestMessage.getMessageId(), mapper.pageToPageDto(page)));
		}
		catch(ProjectNotLoadedException _)
		{
			final ProjectNotLoadedError error = new ProjectNotLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}
		catch(PageNotExistsException _)
		{
			final PageNotExistsError error = new PageNotExistsError(requestMessage.getPageId());
			throw new PlayWallServerException(messageSource, error);
		}
	}
}
