package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.event.KeyInputEvent;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.action.ActionHandler;
import de.thecodelabs.midi.mapping.feedback.FeedbackState;

public class PageActionHandler implements ActionHandler
{
	@Override
	public FeedbackState handleAction(KeyInputEvent keyInputEvent, Action action)
	{
		return null;
	}

	@Override
	public FeedbackState getCurrentState(Action action)
	{
		return null;
	}
}
