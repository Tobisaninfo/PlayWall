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
import de.tobias.playwall.client.domain.pad.PadStatus;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.UUID;


@Service
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class PadActionHandler implements ActionHandler
{
	private final ClientProjectController clientProjectController;
	private final FluentClient client;

	@Override
	public void handleAction(KeyInputEvent keyInputEvent, Action action)
	{
		if(!(action instanceof PadAction padAction))
		{
			throw new IllegalArgumentException("Action is not a PadAction");
		}

		if(padAction.getPosition() == null)
		{
			return;
		}

		if(keyInputEvent.keyInputType() != KeyInputType.UP && padAction.getPadActionMode() != PadAction.PadActionMode.PLAY_HOLD)
		{
			return;
		}

		final UUID pageId = padAction.getPageId();

		final Page page;
		if(pageId == PadAction.ACTIVE_PAGE_ID)
		{
			page = clientProjectController.getCurrentPage();
		}
		else
		{
			page = clientProjectController.getProject().getPage(pageId);
		}

		if(page == null)
		{
			return;
		}

		final ClientPadController padController = findPadController(padAction, page);
		if(padController == null)
		{
			return;
		}

		try
		{
			switch(padAction.getPadActionMode())
			{
				case PLAY_STOP -> onPlayStop(padController);
				case PLAY_PAUSE -> onPlayPause(padController);
				case PLAY_PLAY -> onPlayPlay(padController);
				case PLAY_HOLD -> onPlayHold(keyInputEvent, padController);
			}
		}
		catch(PlayWallApiException e)
		{
			throw new RuntimeException(e);
		}
	}

	private ClientPadController findPadController(PadAction padAction, Page page)
	{
		final Pad pad = page.getPad(padAction.getPosition());
		if(pad == null)
		{
			return null;
		}

		final ClientPadController padController = clientProjectController.getPadController(pad.getId());
		if(padController == null)
		{
			return null;
		}

		final PadStatus status = padController.getStatus();
		if(status == null || status == PadStatus.ERROR || status == PadStatus.EMPTY)
		{
			return null;
		}

		return padController;
	}

	private void onPlayStop(ClientPadController padController) throws PlayWallApiException
	{
		if(padController.getStatus().isAnyPlayingState())
		{
			client.pad(padController.getPad().getId()).stop();
		}
		else
		{
			client.pad(padController.getPad().getId()).play();
		}
	}

	private void onPlayPause(ClientPadController padController) throws PlayWallApiException
	{
		if(padController.getStatus().isAnyPlayingState())
		{
			client.pad(padController.getPad().getId()).pause();
		}
		else
		{
			client.pad(padController.getPad().getId()).play();
		}
	}

	private void onPlayPlay(ClientPadController padController) throws PlayWallApiException
	{
		client.pad(padController.getPad().getId()).stopImmediately();
		client.pad(padController.getPad().getId()).play();
	}

	private void onPlayHold(KeyInputEvent keyInputEvent, ClientPadController padController) throws PlayWallApiException
	{
		if(keyInputEvent.keyInputType() == KeyInputType.DOWN)
		{
			client.pad(padController.getPad().getId()).play();
		}
		else
		{
			client.pad(padController.getPad().getId()).stop();
		}
	}

	@Override
	public FeedbackState getCurrentState(Action action)
	{
		return null;
	}
}
