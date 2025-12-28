package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.api.project.PageAddRequest;
import de.tobias.playwall.common.api.project.PageAddResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.PageMapper;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Page;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(PageAddRequest.class)
public class PageAddHandler implements RequestHandler<PageAddRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final PageMapper mapper;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(PageAddRequest requestMessage) throws IOException, PlayWallServerException
	{
		try
		{
			final Page page = projectService.addPage(projectController.getLoadedProject(), requestMessage.getName());
			return Optional.of(new PageAddResponse(requestMessage.getMessageId(), mapper.pageToPageDto(page)));
		}
		catch(ProjectNotLoadedException _)
		{
			final ProjectNotLoadedError error = new ProjectNotLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}
	}
}
