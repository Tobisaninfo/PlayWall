package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageReplaceRequest;
import de.tobias.playwall.common.api.page.update.PageReplaceUpdate;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

@AllArgsConstructor
@RequestHandlerTyped(PageReplaceRequest.class)
class PageReplaceHandler implements OneTimeActionRequestHandler<PageReplaceRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final PageMapper mapper;
	private final ApplicationContext context;

	@Override
	public void handleRequest(PageReplaceRequest requestMessage) throws PlayWallServerException
	{
			final Project project = projectController.getLoadedProject();
			projectController.unloadAndRemovePage(project.getPages().get(requestMessage.getIndex()));

			final Page page = mapper.pageDtoToPage(requestMessage.getPage());
			projectService.replacePage(project, page, requestMessage.getIndex());
			context.publishEvent(new PageReplaceUpdate(mapper.pageToPageDto(page), requestMessage.getIndex()));

			projectController.loadPage(page);
	}
}
