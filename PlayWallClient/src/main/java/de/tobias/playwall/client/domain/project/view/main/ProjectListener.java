package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMapper;
import de.tobias.playwall.client.event.EventListener;
import de.tobias.playwall.common.api.project.update.ProjectPageShowCommand;
import de.tobias.playwall.common.api.project.update.ProjectUpdate;
import javafx.application.Platform;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProjectListener
{
	private final ProjectMapper projectMapper;
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
}
