package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.project.update.ProjectLoadedUpdate;
import de.tobias.playwall.server.api.history.UndoManager;
import de.tobias.playwall.server.api.pad.PadNotExistsException;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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
	private final UndoManager undoManager;

	private final Map<UUID, PadController> padControllers = new HashMap<>();

	@Async
	public CompletableFuture<Void> loadProject(Project project)
	{
		unloadProject();
		loadedProject = project;
		loadPads();
		return CompletableFuture.completedFuture(null);
	}

	public void unloadProject()
	{
		unloadPads();
		loadedProject = null;
		undoManager.clear();
	}

	private void unloadPads()
	{
		padControllers.values().forEach(PadController::unload);
		padControllers.clear();
	}

	public void loadPage(Page page)
	{
		page.getPads().stream()
				.filter(pad -> pad.getContent() != null)
				.forEach(pad -> {
					final PadController controller = createNewPadController(pad);
					controller.load();
				});
	}

	public void unloadAndRemovePadControllersForPage(Page page)
	{
		page.getPads()
				.stream().filter(pad -> padControllers.containsKey(pad.getId()))
				.forEach(pad -> {
					final PadController controller = padControllers.get(pad.getId());
					controller.unload();
					padControllers.remove(pad.getId());
				});
	}

	public void unloadAndRemovePadController(UUID padId)
	{
		final PadController controller = padControllers.get(padId);
		if(controller != null)
		{
			controller.unload();
			padControllers.remove(padId);
		}
	}

	private void loadPads()
	{
		getLoadedProject()
				.getPages().stream()
				.flatMap(page -> page.getPads().stream())
				.filter(pad -> pad.getContent() != null)
				.forEach(this::createNewPadController);

		padControllers.values().forEach(PadController::load);
		context.publishEvent(new ProjectLoadedUpdate());
	}

	public boolean isAnyProjectLoaded()
	{
		return loadedProject != null;
	}

	public @NonNull Project getLoadedProject() throws ProjectNotLoadedException
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

	public @NonNull Pad getPad(UUID padId)
	{
		final Project project = getLoadedProject();
		final Pad pad = project.getPad(padId);
		if(pad == null)
		{
			throw new PadNotExistsException(project.getMetadata().getId(), padId);
		}
		return pad;
	}

	public Page getPageByPad(UUID padId)
	{
		return getLoadedProject().getPageByPad(padId);
	}

	/**
	 * Get all Pads that are in a playing state.
	 */
	public List<PadController> getPlayingPadControllers()
	{
		return this.padControllers.values().stream()
				.filter(controller -> controller.getStatus() == PadControllerStatus.PLAYING
				                      || controller.getStatus() == PadControllerStatus.PAUSING
				                      || controller.getStatus() == PadControllerStatus.STOPPING)
				.toList();
	}

	public void stopAll()
	{
		getPlayingPadControllers().forEach(PadController::stopImmediately);
	}

	public void setOutputDeviceForAll(String outputDeviceName)
	{
		this.padControllers.values().forEach(p -> {
			p.stop();
			p.setOutputDevice(outputDeviceName);
		});
	}

	public PadController createNewPadController(Pad pad)
	{
		final PadController controller = padControllerFactory.createPadContentController(context, loadedProject, pad);
		padControllers.put(pad.getId(), controller);
		return controller;
	}

	public void replacePad(Pad source, Pad target)
	{
		unloadAndRemovePadController(target.getId());

		final Page targetPage = getPageByPad(target.getId());
		targetPage.replacePad(source, target);

		if(source.getContent() != null)
		{
			createNewPadController(source);
		}
	}

	public void swapPad(Pad pad1, Pad pad2)
	{
		final Page pad1Page = getPageByPad(pad1.getId());
		final Page pad2Page = getPageByPad(pad2.getId());

		pad1Page.removePad(pad1);
		pad2Page.removePad(pad2);

		int position = pad1Page.getPosition();
		pad1.setPosition(pad2.getPosition());
		pad2.setPosition(position);

		pad1Page.insertPad(pad2);
		pad2Page.insertPad(pad1);
	}
}
