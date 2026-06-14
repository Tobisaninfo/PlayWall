package de.tobias.playwall.client.domain.pad.view.desktop.listener.drop;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.view.components.drag.DropOption;
import javafx.scene.input.DragEvent;

public class DuplicatePadDropOption implements DropOption
{
	@Override
	public void handleDrag(DesktopPadView padView, DragEvent event)
	{

	}

	@Override
	public String getLabel()
	{
		return Localization.getString(Strings.UI_PAD_DRAG_DUPLICATE);
	}

	@Override
	public FontAwesomeType getIcon()
	{
		return FontAwesomeType.COPY_SOLID;
	}
}
