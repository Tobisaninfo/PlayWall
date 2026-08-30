package de.tobias.playwall.client.domain.mapping.listener;

import de.thecodelabs.midi.mapping.Mapping;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.midi.MidiCoordinator;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.event.EventListener;
import de.tobias.playwall.common.api.project.update.ProjectPageShownUpdate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
@Service
public class PageFeedbackListener
{
	private final ClientProjectController projectController;
	private final MidiCoordinator midiCoordinator;

	@EventListener(ProjectPageShownUpdate.class)
	void showPageUpdate(ProjectPageShownUpdate command)
	{
		final Mapping activeMapping = projectController.getProject().getMetadata().getActiveMapping();
		midiCoordinator.showCurrentFeedback(activeMapping);
	}
}
