package de.tobias.playwall.client.domain.mapping.listener;

import de.thecodelabs.midi.mapping.Mapping;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.midi.MidiCoordinator;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.event.EventListener;
import de.tobias.playwall.common.api.pad.update.PadStatusUpdate;
import de.tobias.playwall.common.api.pad.update.PadWarningAnimationPlayUpdate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
@Service
public class PadFeedbackListener
{
	private final ClientProjectController projectController;
	private final MidiCoordinator midiCoordinator;

	@EventListener(PadStatusUpdate.class)
	void onPadStatusUpdate(PadStatusUpdate padStatusUpdate)
	{
		final Mapping activeMapping = projectController.getProject().getMetadata().getActiveMapping();
		midiCoordinator.showCurrentFeedback(activeMapping);
	}

	@EventListener(PadWarningAnimationPlayUpdate.class)
	void onPadWarningAnimationPlayUpdate(PadWarningAnimationPlayUpdate padWarningAnimationPlayUpdate)
	{
		final Mapping activeMapping = projectController.getProject().getMetadata().getActiveMapping();
		midiCoordinator.showCurrentFeedback(activeMapping);
	}
}
