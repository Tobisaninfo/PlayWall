package de.tobias.playwall.server.project;

import de.tobias.playwall.server.api.project.model.Project;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ProjectController
{
	private Project loadedProject;

	private final Map<UUID, PadController> padControllers = new HashMap<>();

	@Async
	public void loadProject(Project project)
	{
		unloadPads();
		loadedProject = project;
		loadPads();
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
				.forEach(pad -> padControllers.put(pad.getId(), new PadController(pad)));

		padControllers.values().forEach(PadController::load);
	}
}
