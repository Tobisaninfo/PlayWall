package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
@RequiredArgsConstructor
public class ProjectController
{
	private final ApplicationContext context;
	private final PadContentControllerFactory padControllerFactory;
	private Project loadedProject;

	private final Map<UUID, PadController> padControllers = new HashMap<>();

	@Async
	public CompletableFuture<Void> loadProject(Project project)
	{
		unloadPads();
		loadedProject = project;
		loadPads();
		return CompletableFuture.completedFuture(null);
	}

	public void unloadProject()
	{
		unloadPads();
		loadedProject = null;
	}

	private void unloadPads()
	{
		padControllers.values().forEach(PadController::unload);
		padControllers.clear();
	}

	private void loadPads()
	{
		loadedProject.getPages().stream()
				.flatMap(page -> page.getPads().stream())
				.filter(pad -> pad.getContent() != null)
				.forEach(this::createNewPadController);

		padControllers.values().forEach(PadController::load);
	}

	public Project getLoadedProject() throws ProjectNotLoadedException
	{
		if(loadedProject == null)
		{
			throw new ProjectNotLoadedException();
		}
		return loadedProject;
	}

	public PadController getPadController(UUID padId)
	{
		return padControllers.get(padId);
	}

	public Pad getPad(UUID padId)
	{
		return loadedProject.getPad(padId);
	}

	public Page getPageByPad(UUID padId)
	{
		return loadedProject.getPageByPad(padId);
	}

	public List<PadController> getPlayingPadControllers()
	{
		return this.padControllers.values().stream()
				.filter(controller -> controller.getStatus() == PadControllerStatus.PLAY)
				.toList();
	}

	public PadController createNewPadController(Pad pad)
	{
		final PadController controller = padControllerFactory.createPadContentController(context, pad);
		padControllers.put(pad.getId(), controller);
		return controller;
	}
}
