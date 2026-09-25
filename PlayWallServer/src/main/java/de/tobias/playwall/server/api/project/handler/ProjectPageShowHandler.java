package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectPageShowRequest;
import de.tobias.playwall.common.api.project.update.ProjectPageShownUpdate;
import de.tobias.playwall.server.api.project.PageIndexOutOfRangeException;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@RequestHandlerTyped(ProjectPageShowRequest.class)
@RequiredArgsConstructor
class ProjectPageShowHandler implements OneTimeActionRequestHandler<ProjectPageShowRequest>
{
	private final ProjectController projectController;
	private final ApplicationContext context;

	@Override
	public void handleRequest(ProjectPageShowRequest requestMessage) throws IOException
	{
		final Project loadedProject = projectController.getLoadedProject();

		final int index = requestMessage.getIndex();
		final int pageCount = loadedProject.getPages().size();
		if(index < 0 || index >= pageCount)
		{
			throw new PageIndexOutOfRangeException(index, pageCount);
		}

		projectController.setCurrentPageIndex(index);
		context.publishEvent(new ProjectPageShownUpdate(index));
	}
}
