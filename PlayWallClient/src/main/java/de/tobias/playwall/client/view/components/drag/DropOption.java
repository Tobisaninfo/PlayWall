package de.tobias.playwall.client.view.components.drag;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.scene.input.DragEvent;

public interface DropOption extends Comparable<DropOption>
{
	void handleDrag(DesktopPadView padView, DragEvent event);

	String getLabel();

	FontAwesomeType getIcon();

	default int compareTo(DropOption other)
	{
		return getLabel().compareTo(other.getLabel());
	}
}
