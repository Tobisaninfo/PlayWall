package de.tobias.playwall.server.project;

import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.project.PadController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

	public PadController createNewPadController(Pad pad)
	{
		final PadController controller = padControllerFactory.createPadContentController(context, pad);
		padControllers.put(pad.getId(), controller);
		return controller;
	}
}
