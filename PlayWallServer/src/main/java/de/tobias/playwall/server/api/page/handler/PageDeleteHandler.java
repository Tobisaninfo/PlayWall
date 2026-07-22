package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageDeleteRequest;
import de.tobias.playwall.common.api.page.request.PageInsertRequest;
import de.tobias.playwall.common.api.page.request.PageReplaceRequest;
import de.tobias.playwall.common.api.page.update.PageAddUpdate;
import de.tobias.playwall.common.api.page.update.PageDeleteUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.api.page.PageNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Optional;

@RequestHandlerTyped(PageDeleteRequest.class)
class PageDeleteHandler extends UndoableRequestHandler<PageDeleteRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final PageMapper pageMapper;

	PageDeleteHandler(MessageSource messageSource, ApplicationContext context, ProjectController projectController, ProjectService projectService, PageMapper pageMapper)
	{
		super(messageSource, context);
		this.projectController = projectController;
		this.projectService = projectService;
		this.pageMapper = pageMapper;
	}

	@Override
	public Optional<UndoItem> handleRequest(PageDeleteRequest requestMessage)
	{
		final Project project = projectController.getLoadedProject();
		final Optional<Page> pageOptional = project.getPageById(requestMessage.getPageId());
		if(pageOptional.isEmpty())
		{
			throw new PageNotExistsException(project.getMetadata().getId(), requestMessage.getPageId());
		}

		final Page page = pageOptional.get();
		final String pageName = page.getSettings().getName();

		final String shortDescription = messageSource.getMessage("undo.description.short.page.delete", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.page.delete", new Object[]{pageName}, LocaleContextHolder.getLocale());

		// Stop playing pads on the page
		projectController.unloadAndRemovePage(page);

		// Delete pad controllers
		projectController.deletePadControllersForPage(page);

		// Remove the page from the project
		final boolean success = projectService.deletePage(project, requestMessage.getPageId());
		if(!success)
		{
			throw new PageNotExistsException(project.getMetadata().getId(), requestMessage.getPageId());
		}

		context.publishEvent(new PageDeleteUpdate(requestMessage.getPageId(), project.getPagePositions()));

		// Create new page if last page is deleted
		if(project.getPages().isEmpty())
		{
			final Page newPage = projectService.addPage(project);
			context.publishEvent(new PageAddUpdate(pageMapper.pageToPageDto(newPage)));
			return Optional.of(new UndoItem(shortDescription, longDescription, requestMessage, new PageReplaceRequest(page.getPosition(), pageMapper.pageToPageDto(page))));
		}
		else
		{
			return Optional.of(new UndoItem(shortDescription, longDescription, requestMessage, new PageInsertRequest(page.getPosition(), pageMapper.pageToPageDto(page))));
		}
	}
}
