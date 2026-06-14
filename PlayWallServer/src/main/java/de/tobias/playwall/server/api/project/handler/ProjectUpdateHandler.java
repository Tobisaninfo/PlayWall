package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectUpdateRequest;
import de.tobias.playwall.common.api.project.update.ProjectUpdate;
import de.tobias.playwall.server.api.project.ProjectMapper;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * Update pads of the loaded project based on pads from the request. The Pages must be identical. Pads only existing
 * in the request will be added, existing pads will be updated on position property, and pads that do not exist in the
 * request get removed from the loaded project.
 */
@RequestHandlerTyped(ProjectUpdateRequest.class)
class ProjectUpdateHandler implements OneTimeActionRequestHandler<ProjectUpdateRequest>
{
	private final ProjectMapper projectMapper;
	private final ProjectController projectController;

	private final Executor asyncExecutor;
	private final ApplicationEventPublisher eventPublisher;

	ProjectUpdateHandler(ProjectMapper projectMapper, ProjectController projectController, @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME) Executor asyncExecutor, ApplicationEventPublisher eventPublisher)
	{
		this.projectMapper = projectMapper;
		this.projectController = projectController;
		this.asyncExecutor = asyncExecutor;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void handleRequest(ProjectUpdateRequest requestMessage) throws IOException
	{
		final List<Pad> padsToBeLoaded = new ArrayList<>();

		final Project newProject = projectMapper.projectDtoToProject(requestMessage.getProject());
		final Project oldProject = projectController.getLoadedProject();

		for(Page newPage : newProject.getPages())
		{
			final Page oldPage = oldProject.getPageById(newPage.getId()).orElseThrow();

			// Add new pads or update position of existing pads
			for(Pad newPad : newPage.getPads())
			{
				final Optional<Pad> oldPadOptional = oldPage.getPad(newPad.getId());
				if(oldPadOptional.isPresent())
				{
					oldPadOptional.get().setPosition(newPad.getPosition());
				}
				else
				{
					oldPage.getPads().add(newPad);
					padsToBeLoaded.add(newPad);
				}
			}

			// Unload and remove old pads
			final Iterator<Pad> padIterator = oldPage.getPads().iterator();
			while(padIterator.hasNext())
			{
				final Pad oldPad = padIterator.next();
				if(newPage.getPad(oldPad.getId()).isEmpty())
				{
					projectController.unloadAndRemovePadController(oldPad.getId());
					padIterator.remove();
				}
			}

			oldPage.getPads().sort(Comparator.comparing(Pad::getPosition));
		}

		eventPublisher.publishEvent(new ProjectUpdate(projectMapper.projectToProjectDto(oldProject)));

		// Load content of new pads
		for(Pad pad : padsToBeLoaded)
		{
			if(pad.getContent() != null)
			{
				final PadController controller = projectController.createNewPadController(pad);
				asyncExecutor.execute(controller::load);
			}
		}
	}
}
