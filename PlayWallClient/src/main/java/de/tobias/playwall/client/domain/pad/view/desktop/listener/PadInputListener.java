package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;

public abstract class PadInputListener
{
	public void onMouseClicked(DesktopPadView padView, MouseEvent event)
	{
	}

	public void onDragDetected(DesktopPadView padView, MouseEvent event)
	{
	}

	public void onMouseDragEntered(DesktopPadView padView, MouseEvent event)
	{
	}

	public void onDragOver(DesktopPadView padView, DragEvent event)
	{
	}

	public void onDragDropped(DesktopPadView padView, DragEvent event)
	{
	}

	public void onDragExited(DesktopPadView padView, DragEvent event)
	{
	}

	public void onMouseReleased(DesktopPadView padView, MouseEvent event)
	{
	}
}
