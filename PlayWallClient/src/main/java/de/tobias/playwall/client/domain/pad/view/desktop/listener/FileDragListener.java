package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.scene.input.DragEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileDragListener extends PadInputListener
{
	@Override
	public void onDragOver(DesktopPadView padView, DragEvent event)
	{
		if(event.getDragboard().hasFiles())
		{
			event.consume();
		}
	}

	@Override
	public void onMouseDragExited(DesktopPadView padView, DragEvent event)
	{
		if(event.getDragboard().hasFiles())
		{
			event.consume();
		}
	}

	@Override
	public void onDragDropped(DesktopPadView padView, DragEvent event)
	{
		if(event.getDragboard().hasFiles())
		{
			event.consume();
		}
	}
}
