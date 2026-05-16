package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageDeleteRequest;
import de.tobias.playwall.common.api.page.request.PageDuplicateRequest;
import de.tobias.playwall.common.api.page.update.PageInsertUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(PageDuplicateRequest.class)
class PageDuplicateHandler extends UndoableRequestHandler<PageDuplicateRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final PageMapper mapper;

	PageDuplicateHandler(MessageSource messageSource, ApplicationContext context, ProjectController projectController, ProjectService projectService, PageMapper mapper)
	{
		super(messageSource, context);
		this.projectController = projectController;
		this.projectService = projectService;
		this.mapper = mapper;
	}

	@Override
	public Optional<UndoItem> handleRequest(PageDuplicateRequest requestMessage) throws IOException
	{
		final String shortDescription = messageSource.getMessage("undo.description.short.page.duplicate", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.page.duplicate", new Object[]{}, LocaleContextHolder.getLocale());

		final Project project = projectController.getLoadedProject();
		final Page page = projectService.duplicatePage(project, requestMessage.getPageId());

		context.publishEvent(new PageInsertUpdate(mapper.pageToPageDto(page), page.getPosition(), project.getPagePositions()));
		projectController.loadPage(page);

		return Optional.of(new UndoItem(shortDescription, longDescription, requestMessage, new PageDeleteRequest(page.getId())));
	}
}
