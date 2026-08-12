package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.event.KeyInputEvent;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.action.ActionHandler;
import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StopAllActionHandler implements ActionHandler
{
	@Override
	public FeedbackState handleAction(KeyInputEvent keyInputEvent, Action action)
	{
		final FluentClient client = AppContextHolder.getInstance().get(FluentClient.class);

		try
		{
			client.currentProject().stopAllPads();
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot perform action: StopAllAction", e);
		}

		return null;
	}

	@Override
	public FeedbackState getCurrentState(Action action)
	{
		return null;
	}
}
