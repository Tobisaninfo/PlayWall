package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageDeleteRequest;
import de.tobias.playwall.common.api.page.request.PageNotExistsError;
import de.tobias.playwall.common.api.page.update.PageAddUpdate;
import de.tobias.playwall.common.api.page.update.PageDeleteUpdate;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@RequestHandlerTyped(PageDeleteRequest.class)
class PageDeleteHandler implements GetRequestHandler<PageDeleteRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final PageMapper pageMapper;
	private final ApplicationContext context;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(PageDeleteRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Project project = projectController.getLoadedProject();
			final Optional<Page> pageOptional = project.getPageById(requestMessage.getPageId());
			if(pageOptional.isEmpty())
			{
				final PageNotExistsError error = new PageNotExistsError(requestMessage.getPageId());
				throw new PlayWallServerException(messageSource, error);
			}

			// Stop playing pads on the page
			final Page page = pageOptional.get();
			page.getPads().forEach(pad -> {
				final PadController padController = projectController.getPadController(pad.getId());
				if(padController != null)
				{
					padController.stop();
					padController.unload();
				}
			});

			// Delete pad controllers
			projectController.deletePadControllersForPage(page);

			// Remove the page from the project
			final boolean success = projectService.deletePage(project, requestMessage.getPageId());
			if(success)
			{
				context.publishEvent(new PageDeleteUpdate(requestMessage.getPageId(), project.getPages().stream().collect(Collectors.toMap(
						Page::getId,
						Page::getPosition
				))));

				// Create new page if last page is deleted
				if(project.getPages().isEmpty())
				{
					final Page newPage = projectService.addPage(project);
					context.publishEvent(new PageAddUpdate(pageMapper.pageToPageDto(newPage)));
				}

				return Optional.empty();
			}

			final PageNotExistsError error = new PageNotExistsError(requestMessage.getPageId());
			throw new PlayWallServerException(messageSource, error);
		}
		catch(ProjectNotLoadedException _)
		{
			final ProjectNotLoadedError error = new ProjectNotLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}
	}
}
