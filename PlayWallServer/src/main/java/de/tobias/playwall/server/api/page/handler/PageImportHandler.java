package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageDeleteRequest;
import de.tobias.playwall.common.api.page.request.PageImportRequest;
import de.tobias.playwall.common.api.page.update.PageAddUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.api.page.PageService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@RequestHandlerTyped(PageImportRequest.class)
class PageImportHandler extends UndoableRequestHandler<PageImportRequest>
{
	private final ProjectController projectController;
	private final PageService pageService;
	private final PageMapper pageMapper;

	public PageImportHandler(MessageSource messageSource, ApplicationContext context, ProjectController projectController, PageService pageService, PageMapper pageMapper)
	{
		super(messageSource, context);
		this.projectController = projectController;
		this.pageService = pageService;
		this.pageMapper = pageMapper;
	}

	@Override
	public Optional<UndoItem> handleRequest(PageImportRequest requestMessage) throws IOException
	{
		final String shortDescription = messageSource.getMessage("undo.description.short.page.import", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.page.import", new Object[]{}, LocaleContextHolder.getLocale());

		final byte[] data = Base64.getDecoder().decode(requestMessage.getBase64());
		final Project project = projectController.getLoadedProject();

		final Page page = pageService.importPage(project, data, requestMessage.getMimetype());
		context.publishEvent(new PageAddUpdate(pageMapper.pageToPageDto(page)));
		projectController.loadPage(page);

		return Optional.of(new UndoItem(shortDescription, longDescription, requestMessage, new PageDeleteRequest(page.getId())));
	}
}