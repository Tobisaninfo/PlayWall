package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.PageNameAlreadyExistsError;
import de.tobias.playwall.common.api.page.request.PageNotExistsError;
import de.tobias.playwall.common.api.page.request.PageRenameRequest;
import de.tobias.playwall.common.api.page.update.PageRenameUpdate;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.page.PageNameAlreadyExistsException;
import de.tobias.playwall.server.api.page.PageNotExistsException;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@RequestHandlerTyped(PageRenameRequest.class)
class PageRenameHandler implements UndoableRequestHandler<PageRenameRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final MessageSource messageSource;
	private final ApplicationContext context;

	@Override
	public Optional<UndoItem> handleRequest(PageRenameRequest requestMessage) throws IOException, PlayWallServerException
	{
		final String shortDescription = messageSource.getMessage("undo.description.short.page.rename", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.page.rename", new Object[]{}, LocaleContextHolder.getLocale());

		final UUID pageId = requestMessage.getPageId();
		try
		{

			final Project project = projectController.getLoadedProject();
			final String oldName = project.getPageById(pageId).map(Page::getName).orElse("");

			projectService.renamePage(project, pageId, requestMessage.getNewName());

			context.publishEvent(new PageRenameUpdate(pageId, requestMessage.getNewName()));
			return Optional.of(new UndoItem(shortDescription, longDescription, requestMessage, new PageRenameRequest(pageId, oldName)));
		}
		catch(PageNameAlreadyExistsException _)
		{
			final PageNameAlreadyExistsError error = new PageNameAlreadyExistsError(requestMessage.getNewName());
			throw new PlayWallServerException(messageSource, error);
		}
		catch(ProjectNotLoadedException _)
		{
			final ProjectNotLoadedError error = new ProjectNotLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}
		catch(PageNotExistsException _)
		{
			final PageNotExistsError error = new PageNotExistsError(pageId);
			throw new PlayWallServerException(messageSource, error);
		}
	}
}
