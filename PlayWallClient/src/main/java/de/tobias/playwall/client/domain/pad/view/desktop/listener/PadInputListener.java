package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.scene.input.MouseEvent;

public interface PadInputListener
{
	void onMouseClicked(DesktopPadView padView, MouseEvent event);

	void onDragDetected(DesktopPadView padView, MouseEvent event);

	void onMouseDragEntered(DesktopPadView padView, MouseEvent event);
}
