package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMapper;
import de.tobias.playwall.client.event.EventListener;
import de.tobias.playwall.common.api.project.update.ProjectPageShowCommand;
import de.tobias.playwall.common.api.project.update.ProjectPageShownUpdate;
import de.tobias.playwall.common.api.project.update.ProjectUpdate;
import javafx.application.Platform;
import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public class ProjectListener
{
	private final ProjectMapper projectMapper;
	private final ClientProjectController projectController;
	private final MainViewController mainViewController;

	@EventListener(ProjectUpdate.class)
	void onProjectUpdate(ProjectUpdate message)
	{
		final Project project = projectMapper.projectDtoToProject(message.getProject());

		mainViewController.updateProject(project);
	}

	@EventListener(ProjectPageShowCommand.class)
	void showPage(ProjectPageShowCommand command)
	{
		Platform.runLater(() -> mainViewController.showPage(command.getIndex()));
	}

	/**
	 * Reacts to the page shown elsewhere (server-broadcast, e.g. by the Companion plugin in "sync"
	 * mode) changing. {@code ProjectPageShownUpdate} is also fired locally by {@code showPage()}
	 * itself (for MIDI feedback) and echoed back by the server, so this only acts when the index
	 * actually differs from what's already shown — otherwise every local page change would loop
	 * forever (render → notify server → broadcast → apply → render → ...).
	 */
	@EventListener(ProjectPageShownUpdate.class)
	void onPageShownElsewhere(ProjectPageShownUpdate update)
	{
		final Page currentPage = projectController.getCurrentPage();
		if(currentPage != null && Objects.equals(currentPage.getPosition(), update.getIndex()))
		{
			return;
		}

		Platform.runLater(() -> mainViewController.applyPageShownByServer(update.getIndex()));
	}
}
