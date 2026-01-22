package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageAddRequest;
import de.tobias.playwall.common.api.page.update.PageAddUpdate;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(PageAddRequest.class)
class PageAddHandler implements GetRequestHandler<PageAddRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final PageMapper mapper;
	private final ApplicationContext context;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(PageAddRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Page page = projectService.addPage(projectController.getLoadedProject());
			context.publishEvent(new PageAddUpdate(mapper.pageToPageDto(page)));
			return Optional.empty();
		}
		catch(ProjectNotLoadedException _)
		{
			final ProjectNotLoadedError error = new ProjectNotLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}
	}
}
