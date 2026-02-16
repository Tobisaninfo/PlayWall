package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageAddRequest;
import de.tobias.playwall.common.api.page.request.PageDeleteRequest;
import de.tobias.playwall.common.api.page.update.PageAddUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(PageAddRequest.class)
class PageAddHandler implements UndoableRequestHandler<PageAddRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final PageMapper mapper;
	private final ApplicationContext context;
	private final MessageSource messageSource;

	@Override
	public Optional<UndoItem> handleRequest(PageAddRequest requestMessage) throws IOException
	{
		final Page page = projectService.addPage(projectController.getLoadedProject());
		context.publishEvent(new PageAddUpdate(mapper.pageToPageDto(page)));

		return Optional.of(getInverseOperation(requestMessage, page));
	}

	private UndoItem getInverseOperation(PageAddRequest request, Page newPage)
	{
		final String shortDescription = messageSource.getMessage("undo.description.short.page.add", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.page.add", new Object[]{newPage.getName()}, LocaleContextHolder.getLocale());

		return new UndoItem(shortDescription, longDescription, request, new PageDeleteRequest(newPage.getId()));
	}
}
