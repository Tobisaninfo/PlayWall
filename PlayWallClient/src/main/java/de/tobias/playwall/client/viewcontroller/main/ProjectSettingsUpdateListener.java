package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.client.domain.project.ProjectMetadataMapper;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import javafx.application.Platform;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class ProjectSettingsUpdateListener implements UpdateMessageEventListener<ProjectSettingsUpdate>
{
	private final ClientProjectController projectController;
	private final MainViewController mainViewController;
	private final ProjectMetadataMapper projectMetadataMapper;

	@Override
	public void onUpdateMessage(ProjectSettingsUpdate message)
	{
		final ProjectMetadata projectMetadata = projectMetadataMapper.projectMetadataDtoToProjectMetadata(message.getProjectMetadata());
		projectController.updateMetadata(projectMetadata);

		for(PadView padView : mainViewController.getPadViews())
		{
			padView.updateTimeNodes();
		}

		Platform.runLater(mainViewController::updateTitle);
	}

	@Override
	public Class<ProjectSettingsUpdate> getMessageClass()
	{
		return ProjectSettingsUpdate.class;
	}
}
