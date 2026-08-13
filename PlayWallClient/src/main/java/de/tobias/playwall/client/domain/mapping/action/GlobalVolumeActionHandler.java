package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.event.KeyInputEvent;
import de.thecodelabs.midi.event.KeyInputType;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.action.ActionHandler;
import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class GlobalVolumeActionHandler implements ActionHandler
{
	private static final Double MIN_VOLUME = 0.0;
	private static final Double MAX_VOLUME = 1.0;

	private final FluentClient client;

	private final ClientProjectController projectController;

	@Override
	public FeedbackState handleAction(KeyInputEvent keyInputEvent, Action action)
	{
		if(keyInputEvent.keyInputType() != KeyInputType.UP)
		{
			return null;
		}
		if(!(action instanceof GlobalVolumeAction globalVolumeAction))
		{
			throw new IllegalArgumentException("Action is not a GlobalVolumeAction");
		}

		final Double volume = projectController.getProject().getMetadata().getVolume();

		final Double newVolume = switch(globalVolumeAction.getVolumeChangeMode())
		{
			case INCREASE -> Math.min(volume + globalVolumeAction.getDelta().getDelta(), MAX_VOLUME);
			case DECREASE -> Math.max(volume - globalVolumeAction.getDelta().getDelta(), MIN_VOLUME);
		};

		if(volume.equals(newVolume))
		{
			return null;
		}

		try
		{
			client.currentProject().changeGlobalVolume(newVolume);
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot perform action: GlobalVolumeAction", e);
		}

		return null;
	}

	@Override
	public FeedbackState getCurrentState(Action action)
	{
		return null;
	}
}
