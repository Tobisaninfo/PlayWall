package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.common.api.project.update.ProjectLoadedUpdate;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProjectLoadedListener implements UpdateMessageEventListener<ProjectLoadedUpdate>
{
	private final MainViewController mainViewController;

	@Override
	public void onUpdateMessage(ProjectLoadedUpdate message)
	{
		mainViewController.getLoadingOverlay().hide();
	}

	@Override
	public Class<ProjectLoadedUpdate> getMessageClass()
	{
		return ProjectLoadedUpdate.class;
	}
}
