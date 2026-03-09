package de.tobias.playwall.client.view.components.drag;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.scene.input.DragEvent;

public interface FileDragOption extends Comparable<FileDragOption>
{
	void handleDrag(DesktopPadView padView, DragEvent event);

	String getLabel();

	FontAwesomeType getIcon();

	default int compareTo(FileDragOption other)
	{
		return getLabel().compareTo(other.getLabel());
	}
}
