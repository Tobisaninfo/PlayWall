package de.tobias.playwall.client.domain.project.view.main;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.client.view.MaterialToastManager;
import de.tobias.playwall.common.api.history.UndoHistoryUpdate;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class UndoHistoryUpdateListener implements UpdateMessageEventListener<UndoHistoryUpdate>
{
	private MainViewController controller;

	@Override
	public void onUpdateMessage(UndoHistoryUpdate message)
	{
		controller.getUndoMenuItem().setDisable(message.getNextUndoOperation() == null);
		if(message.getNextUndoOperation() != null)
		{
			controller.getUndoMenuItem().setText(Localization.getString(Strings.UI_MENU_EDIT_UNDO_DESCRIPTION, message.getNextUndoOperation()));
		}
		else
		{
			controller.getUndoMenuItem().setText(Localization.getString(Strings.UI_MENU_EDIT_UNDO));
		}

		controller.getRedoMenuItem().setDisable(message.getNextRedoOperation() == null);
		if(message.getNextRedoOperation() != null)
		{
			controller.getRedoMenuItem().setText(Localization.getString(Strings.UI_MENU_EDIT_REDO_DESCRIPTION, message.getNextRedoOperation()));
		}
		else
		{
			controller.getRedoMenuItem().setText(Localization.getString(Strings.UI_MENU_EDIT_REDO));
		}

		if(message.getMessage() != null)
		{
			controller.showNotification(message.getMessage(), MaterialToastManager.ToastType.INFO);
		}
	}

	@Override
	public Class<UndoHistoryUpdate> getMessageClass()
	{
		return UndoHistoryUpdate.class;
	}
}
