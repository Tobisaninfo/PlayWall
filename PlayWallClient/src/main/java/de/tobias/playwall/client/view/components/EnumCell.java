package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.util.Localization;
import javafx.scene.control.ListCell;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnumCell<T extends Enum<?>> extends ListCell<T>
{
	private final String baseName;

	@Override
	protected void updateItem(T item, boolean empty)
	{
		super.updateItem(item, empty);
		if(empty)
		{
			setText("");
		}
		else
		{
			if(item == null)
			{
				setText(Localization.getString(baseName + "null"));
			}
			else
			{
				setText(Localization.getString(baseName + item.name()));
			}
		}
	}
}
