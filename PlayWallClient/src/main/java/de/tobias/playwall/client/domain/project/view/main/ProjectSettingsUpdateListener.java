package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.domain.midi.event.MidiDeviceSelected;
import de.tobias.playwall.client.domain.pad.view.PadView;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.project.ProjectMetadataMapper;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import javafx.application.Platform;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class ProjectSettingsUpdateListener implements UpdateMessageEventListener<ProjectSettingsUpdate>
{
	private final ClientProjectController projectController;
	private final MainViewController mainViewController;
	private final ProjectMetadataMapper projectMetadataMapper;
	private final UpdateMessageEventHandler eventHandler;

	@Override
	public void onUpdateMessage(ProjectSettingsUpdate message)
	{
		final ProjectMetadata projectMetadata = projectMetadataMapper.projectMetadataDtoToProjectMetadata(message.getProjectMetadata());
		projectController.updateMetadata(projectMetadata);

		Platform.runLater(() -> {
			for(PadView padView : mainViewController.getPadViews())
			{
				padView.updateTimeNodes();
			}

			mainViewController.updateTitle();
			mainViewController.updateStyle();

			eventHandler.fireEvent(new MidiDeviceSelected(projectMetadata.getMidiDevice()));
			mainViewController.registerMappingListener();

			mainViewController.updateGlobalVolume(projectMetadata.getVolume());
		});
	}

	@Override
	public Class<ProjectSettingsUpdate> getMessageClass()
	{
		return ProjectSettingsUpdate.class;
	}
}
