package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageReorderRequest;
import de.tobias.playwall.common.api.page.update.PageReorderUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@RequestHandlerTyped(PageReorderRequest.class)
public class PageReorderHandler implements UndoableRequestHandler<PageReorderRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final ApplicationContext context;
	private final MessageSource messageSource;

	@Override
	public Optional<UndoItem> handleRequest(PageReorderRequest requestMessage) throws IOException
	{
		final String shortDescription = messageSource.getMessage("undo.description.short.page.reorder", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.page.reorder", new Object[]{}, LocaleContextHolder.getLocale());

		final Map<UUID, Integer> oldPageOrder = projectService.getPageOrder(projectController.getLoadedProject());
		final UndoItem undoItem = new UndoItem(shortDescription, longDescription, requestMessage, new PageReorderRequest(oldPageOrder));

		projectService.reorderPages(projectController.getLoadedProject(), requestMessage.getPositions());

		context.publishEvent(new PageReorderUpdate(requestMessage.getPositions()));

		return Optional.of(undoItem);
	}
}
