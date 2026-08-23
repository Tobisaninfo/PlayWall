package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.event.KeyInputEvent;
import de.thecodelabs.midi.event.KeyInputType;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.action.ActionHandler;
import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class PadActionHandler implements ActionHandler
{
	private final ClientProjectController clientProjectController;

	@Override
	public FeedbackState handleAction(KeyInputEvent keyInputEvent, Action action)
	{
		if(keyInputEvent.keyInputType() != KeyInputType.UP)
		{
			return null;
		}
		if(!(action instanceof PadAction padAction))
		{
			throw new IllegalArgumentException("Action is not a PadAction");
		}

		switch(padAction.getPadActionMode())
		{
			case PLAY_STOP ->
			{
			}
			case PLAY_PAUSE ->
			{
			}
			case PLAY_PLAY ->
			{
			}
			case PLAY_HOLD ->
			{
			}
		}

		return null;
	}

	@Override
	public FeedbackState getCurrentState(Action action)
	{
		return null;
	}
}
