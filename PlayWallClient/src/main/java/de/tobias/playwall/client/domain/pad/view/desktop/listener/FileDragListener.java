package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.view.components.drag.FileDragOption;
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
			padView.getFileDragOptionView().showOptions(List.of(new FileDragOption()
			{
				@Override
				public void handleDrag(DesktopPadView padView, DragEvent event)
				{
					final Dragboard dragboard = event.getDragboard();
					final List<File> files = dragboard.getFiles();
					final Path path = files.getFirst().toPath();
					padView.handleNewMediaPath(path);
				}

				@Override
				public String getLabel()
				{
					return "Audio";
				}

				@Override
				public FontAwesomeType getIcon()
				{
					return FontAwesomeType.MUSIC_SOLID;
				}
			}));
			event.acceptTransferModes(TransferMode.LINK);
			event.consume();
		}
	}

	@Override
	public void onMouseDragExited(DesktopPadView padView, DragEvent event)
	{
		if(event.getDragboard().hasFiles())
		{
			padView.getFileDragOptionView().hide();
			event.consume();
		}
	}

	@Override
	public void onDragDropped(DesktopPadView padView, DragEvent event)
	{
		final Dragboard dragboard = event.getDragboard();
		if(dragboard.hasFiles())
		{
			final FileDragOption fileDragOption = padView.getFileDragOptionView().getSelectedOption();
			if(fileDragOption == null)
			{
				return;
			}
			fileDragOption.handleDrag(padView, event);
			event.setDropCompleted(true);
			event.consume();
		}
	}
}
