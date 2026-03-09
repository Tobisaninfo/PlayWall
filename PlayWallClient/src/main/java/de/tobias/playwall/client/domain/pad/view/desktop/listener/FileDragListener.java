package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class FileDragListener extends PadInputListener
{
	@Override
	public void onDragOver(DesktopPadView padView, DragEvent event)
	{
		if(event.getDragboard().hasFiles())
		{
			event.acceptTransferModes(TransferMode.LINK);
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
		final Dragboard dragboard = event.getDragboard();
		if(dragboard.hasFiles())
		{
			final List<File> files = dragboard.getFiles();
			final Path path = files.getFirst().toPath();
			padView.handleNewMediaPath(path);
			event.setDropCompleted(true);
			event.consume();
		}
	}
}
