package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.view.components.drag.DropOption;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class FileDragListener extends PadInputListener
{
	@Override
	public void onDragOver(DesktopPadView padView, DragEvent event)
	{
		if(event.getDragboard().hasFiles())
		{
			List<DropOption> fileDragOptions = new ArrayList<>();
			final Optional<NewFileDragOption> newMediaDragOption = NewFileDragOption.create(event.getDragboard().getFiles().getFirst().toPath());
			newMediaDragOption.ifPresent(fileDragOptions::add);

			padView.getDropOptionSelect().showOptions(fileDragOptions);
			event.acceptTransferModes(TransferMode.LINK);
			event.consume();
		}
	}

	@Override
	public void onDragExited(DesktopPadView padView, DragEvent event)
	{
		if(event.getDragboard().hasFiles())
		{
			padView.getDropOptionSelect().hide();
			event.consume();
		}
	}

	@Override
	public void onDragDropped(DesktopPadView padView, DragEvent event)
	{
		final Dragboard dragboard = event.getDragboard();
		if(dragboard.hasFiles())
		{
			final DropOption fileDragOption = padView.getDropOptionSelect().getSelectedOption();
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
