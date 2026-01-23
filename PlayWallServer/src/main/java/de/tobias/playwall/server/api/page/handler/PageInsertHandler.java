package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageInsertRequest;
import de.tobias.playwall.common.api.page.update.PageInsertUpdate;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

@AllArgsConstructor
@RequestHandlerTyped(PageInsertRequest.class)
class PageInsertHandler implements OneTimeActionRequestHandler<PageInsertRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final PageMapper mapper;
	private final ApplicationContext context;
	private final MessageSource messageSource;

	@Override
	public void handleRequest(PageInsertRequest requestMessage) throws PlayWallServerException
	{
		try
		{
			final Page page = mapper.pageDtoToPage(requestMessage.getPage());
			final Project project = projectController.getLoadedProject();
			projectService.insertPage(project, page, requestMessage.getIndex());
			context.publishEvent(new PageInsertUpdate(mapper.pageToPageDto(page), requestMessage.getIndex(), project.getPagePositions()));

			projectController.loadPage(page);
		}
		catch(ProjectNotLoadedException _)
		{
			final ProjectNotLoadedError error = new ProjectNotLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}
	}
}
