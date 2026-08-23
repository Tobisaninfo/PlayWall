package de.tobias.playwall.client.domain.mapping.action;

import de.tobias.playwall.client.domain.page.Page;
import javafx.scene.control.ListCell;

class PageCell extends ListCell<Page>
{
	@Override
	protected void updateItem(Page item, boolean empty)
	{
		super.updateItem(item, empty);

		if(empty || item == null)
		{
			setText(null);
			setGraphic(null);
			return;
		}

		setText(item.getSettings().getName());
	}
}