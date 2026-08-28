package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.domain.page.Page;
import javafx.scene.control.ListCell;

class PageCell extends ListCell<PageCell.PageCellData>
{
	public record PageCellData(Page page, boolean isError)
	{
	}

	@Override
	protected void updateItem(PageCell.PageCellData item, boolean empty)
	{
		super.updateItem(item, empty);

		if(empty || item == null)
		{
			setText(null);
			setGraphic(null);
			return;
		}

		if(item.isError())
		{
			final FontIcon icon = new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID);
			icon.getStyleClass().add("warning");
			setGraphic(icon);
		}
		else
		{
			setGraphic(null);
		}
		setText(item.page().getSettings().getName());
	}
}