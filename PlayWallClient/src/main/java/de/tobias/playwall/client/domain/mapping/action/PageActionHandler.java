package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.event.KeyInputEvent;
import de.thecodelabs.midi.event.KeyInputType;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.action.ActionHandler;
import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.midi.feedback.DefaultFeedbackState;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.common.api.project.update.ProjectPageShowCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Objects;


@Service
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class PageActionHandler implements ActionHandler
{
	private final ClientProjectController clientProjectController;
	private final UpdateMessageEventHandler updateMessageEventHandler;

	@Override
	public void handleAction(KeyInputEvent keyInputEvent, Action action)
	{
		if(keyInputEvent.keyInputType() != KeyInputType.UP)
		{
			return;
		}
		if(!(action instanceof PageAction pageAction))
		{
			throw new IllegalArgumentException("Action is not a PageAction");
		}

		final int currentPage = clientProjectController.getCurrentPage().getPosition();
		final int maxPage = clientProjectController.getProject().getPages().size() - 1;

		switch(pageAction.getPageActionMode())
		{
			case PREVIOUS ->
			{
				if(currentPage > 0)
				{
					updateMessageEventHandler.fireEvent(new ProjectPageShowCommand(currentPage - 1));
				}
			}
			case NEXT ->
			{
				if(currentPage < maxPage)
				{
					updateMessageEventHandler.fireEvent(new ProjectPageShowCommand(currentPage + 1));
				}
			}
			case JUMP ->
			{
				int targetPage = pageAction.getPageNumber() - 1;
				if(targetPage < 0 || targetPage > maxPage)
				{
					return;
				}
				updateMessageEventHandler.fireEvent(new ProjectPageShowCommand(targetPage));
			}
		}
	}

	@Override
	public FeedbackState getCurrentState(Action action)
	{
		if((!(action instanceof PageAction pageAction)))
		{
			return null;
		}

		return switch(pageAction.getPageActionMode())
		{
			case NEXT, PREVIOUS -> DefaultFeedbackState.NORMAL;
			case JUMP ->
			{
				if(Objects.equals(clientProjectController.getCurrentPage().getPosition(), pageAction.getPageNumber() - 1))
				{
					yield DefaultFeedbackState.ACTIVE;
				}
				yield DefaultFeedbackState.NORMAL;
			}
		};
	}
}
