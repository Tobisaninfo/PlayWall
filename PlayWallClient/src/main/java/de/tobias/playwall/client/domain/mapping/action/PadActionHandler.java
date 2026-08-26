package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.event.KeyInputEvent;
import de.thecodelabs.midi.event.KeyInputType;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.action.ActionHandler;
import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.PadStatus;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


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

		if(padAction.getPosition() == null)
		{
			return null;
		}

		if(keyInputEvent.keyInputType() != KeyInputType.UP && padAction.getPadActionMode() != PadAction.PadActionMode.PLAY_HOLD)
		{
			return null;
		}

		final UUID pageId = padAction.getPageId();

		final List<Page> pages;
		if(pageId == null)
		{
			pages = clientProjectController.getProject().getPages();
		}
		else
		{
			pages = List.of(clientProjectController.getProject().getPage(pageId));
		}

		if(pages.isEmpty())
		{
			return null;
		}

		final List<ClientPadController> padControllers = determineNonEmptyMatchingPadControllers(padAction, pages);
		if(padControllers.isEmpty())
		{
			return null;
		}

		try
		{
			for(ClientPadController controller : padControllers)
			{
				switch(padAction.getPadActionMode())
				{
					case PLAY_STOP -> onPlayStop(controller);
					case PLAY_PAUSE -> onPlayPause(controller);
					case PLAY_PLAY -> onPlayPlay(controller);
					case PLAY_HOLD -> onPlayHold(keyInputEvent, controller);
				}
			}
		}
		catch(PlayWallApiException e)
		{
			throw new RuntimeException(e);
		}

		return null;
	}

	private List<ClientPadController> determineNonEmptyMatchingPadControllers(PadAction padAction, List<Page> pages)
	{
		return pages.stream()
				.map(p -> p.getPad(padAction.getPosition()))
				.filter(Objects::nonNull)
				.map(pad -> clientProjectController.getPadController(pad.getId()))
				.filter(c -> c.getStatus() != null && c.getStatus() != PadStatus.ERROR && c.getStatus() != PadStatus.EMPTY)
				.toList();
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
