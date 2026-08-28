package de.tobias.playwall.client.domain.project.view.main;

import de.thecodelabs.midi.mapping.Mapping;
import de.tobias.playwall.client.domain.midi.MidiCoordinator;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMapper;
import de.tobias.playwall.client.event.EventListener;
import de.tobias.playwall.common.api.project.update.ProjectPageShowCommand;
import de.tobias.playwall.common.api.project.update.ProjectPageShownUpdate;
import de.tobias.playwall.common.api.project.update.ProjectUpdate;
import javafx.application.Platform;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProjectListener
{
	private final ProjectMapper projectMapper;
	private final MainViewController mainViewController;

	private final ClientProjectController projectController;
	private final MidiCoordinator midiCoordinator;

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

	@EventListener(ProjectPageShownUpdate.class)
	void showPageUpdate(ProjectPageShownUpdate command)
	{
		final Mapping activeMapping = projectController.getProject().getMetadata().getActiveMapping();
		midiCoordinator.showCurrentFeedback(activeMapping);
	}
}
