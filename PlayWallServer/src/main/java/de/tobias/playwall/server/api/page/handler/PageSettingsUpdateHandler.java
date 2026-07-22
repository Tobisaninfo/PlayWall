package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageSettingsUpdateRequest;
import de.tobias.playwall.common.api.page.update.PageSettingsUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.page.PageNotExistsException;
import de.tobias.playwall.server.api.page.PageSettingsMapper;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.page.PageSettings;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@RequestHandlerTyped(PageSettingsUpdateRequest.class)
class PageSettingsUpdateHandler extends UndoableRequestHandler<PageSettingsUpdateRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final PageSettingsMapper pageSettingsMapper;

	PageSettingsUpdateHandler(MessageSource messageSource, ApplicationContext context, ProjectController projectController, ProjectService projectService, PageSettingsMapper pageSettingsMapper)
	{
		super(messageSource, context);
		this.projectController = projectController;
		this.projectService = projectService;
		this.pageSettingsMapper = pageSettingsMapper;
	}

	@Override
	public Optional<UndoItem> handleRequest(PageSettingsUpdateRequest requestMessage) throws IOException
	{
		final String shortDescription = messageSource.getMessage("undo.description.short.page.settings", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.page.settings", new Object[]{}, LocaleContextHolder.getLocale());

		final UUID pageId = requestMessage.getPageId();
		final Project project = projectController.getLoadedProject();

		final Optional<Page> pageOptional = project.getPageById(pageId);
		if(pageOptional.isEmpty())
		{
			throw new PageNotExistsException(project.getMetadata().getId(), pageId);
		}

		final PageSettings pageSettings = pageOptional.get().getSettings();
		final PageSettingsUpdateRequest undoRequest = new PageSettingsUpdateRequest(pageId, pageSettingsMapper.pageSettingsToPageSettingsDto(pageSettings));

		pageSettings.setColor(requestMessage.getPageSettings().color());
		projectService.renamePage(project, pageId, requestMessage.getPageSettings().name());

		context.publishEvent(new PageSettingsUpdate(pageId, pageSettingsMapper.pageSettingsToPageSettingsDto(pageSettings)));
		return Optional.of(new UndoItem(shortDescription, longDescription, requestMessage, undoRequest));
	}
}
