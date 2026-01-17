package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageAddResponse;
import de.tobias.playwall.common.api.page.request.PageDuplicateRequest;
import de.tobias.playwall.common.api.page.request.PageNotExistsError;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.api.page.PageNotExistsException;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(PageDuplicateRequest.class)
class PageDuplicateHandler implements RequestHandler<PageDuplicateRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final PageMapper mapper;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(PageDuplicateRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Project project = projectController.getLoadedProject();
			final Page page = projectService.duplicatePage(project, requestMessage.getPageId(), requestMessage.getName());
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
