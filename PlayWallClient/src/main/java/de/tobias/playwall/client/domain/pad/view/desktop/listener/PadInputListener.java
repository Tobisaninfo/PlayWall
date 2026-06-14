package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;

public interface PadInputListener
{
	default void onMouseClicked(DesktopPadView padView, MouseEvent event)
	{
	}

	default void onDragDetected(DesktopPadView padView, MouseEvent event)
	{
	}

	default void onMouseDragEntered(DesktopPadView padView, MouseEvent event)
	{
	}

	default void onDragOver(DesktopPadView padView, DragEvent event)
	{
	}

	default void onDragDropped(DesktopPadView padView, DragEvent event)
	{
	}

	default void onDragExited(DesktopPadView padView, DragEvent event)
	{
	}

	default void onMouseReleased(DesktopPadView padView, MouseEvent event)
	{
	}
}
