package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.event.KeyInputEvent;
import de.thecodelabs.midi.event.KeyInputType;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.action.ActionHandler;
import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class PadActionHandler implements ActionHandler
{
	private final ClientProjectController clientProjectController;
	private final FluentClient client;

	@Override
	public FeedbackState handleAction(KeyInputEvent keyInputEvent, Action action)
	{
		if(!(action instanceof PadAction padAction))
		{
			throw new IllegalArgumentException("Action is not a PadAction");
		}

		if(keyInputEvent.keyInputType() != KeyInputType.UP && padAction.getPadActionMode() != PadAction.PadActionMode.PLAY_HOLD)
		{
			return null;
		}

		final Page page = clientProjectController.getProject().getPage(padAction.getPageId());
		if(page == null)
		{
			return null;
		}

		// TODO: debug only
		final Pad pad = page.getPad(7);

		final ClientPadController padController = clientProjectController.getPadController(pad.getId());
		if(padController == null)
		{
			return null;
		}

		try
		{
			switch(padAction.getPadActionMode())
			{
				case PLAY_STOP -> onPlayStop(padController, pad);
				case PLAY_PAUSE -> onPlayPause(padController, pad);
				case PLAY_PLAY -> onPlayPlay(pad);
				case PLAY_HOLD -> onPlayHold(keyInputEvent, pad);
			}
		}
		catch(PlayWallApiException e)
		{
			throw new RuntimeException(e);
		}

		return null;
	}

	private void onPlayStop(ClientPadController padController, Pad pad) throws PlayWallApiException
	{
		if(padController.getStatus().isAnyPlayingState())
		{
			client.pad(pad.getId()).stop();
		}
		else
		{
			client.pad(pad.getId()).play();
		}
	}

	private void onPlayPause(ClientPadController padController, Pad pad) throws PlayWallApiException
	{
		if(padController.getStatus().isAnyPlayingState())
		{
			client.pad(pad.getId()).pause();
		}
		else
		{
			client.pad(pad.getId()).play();
		}
	}

	private void onPlayPlay(Pad pad) throws PlayWallApiException
	{
		client.pad(pad.getId()).stopImmediately();
		client.pad(pad.getId()).play();
	}

	private void onPlayHold(KeyInputEvent keyInputEvent, Pad pad) throws PlayWallApiException
	{
		if(keyInputEvent.keyInputType() == KeyInputType.DOWN)
		{
			client.pad(pad.getId()).play();
		}
		else
		{
			client.pad(pad.getId()).stopImmediately();
		}
	}

	@Override
	public FeedbackState getCurrentState(Action action)
	{
		return null;
	}
}
