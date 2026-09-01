package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.pad.view.desktop.listener.drop.PadDropDuplicateOption;
import de.tobias.playwall.client.domain.pad.view.desktop.listener.drop.PadDropMoveOption;
import de.tobias.playwall.client.domain.pad.view.desktop.listener.drop.PadDropSwapOption;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.view.PageButtonInputListener;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import de.tobias.playwall.client.view.components.drag.DropOption;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PadDragListener implements PadInputListener, PageButtonInputListener
{
	private static final String PAD_INDEX_DATATYPE = "de.tobias.playwall.pad_index";
	public static final DataFormat DATA_FORMAT = new DataFormat(PAD_INDEX_DATATYPE);

	private final FluentClient client;
	private final ClientProjectController projectController;
	private final ErrorAlertBuilder errorAlertBuilder;
	private final MainViewController mainViewController;

	@Override
	public void onDragDetected(DesktopPadView padView, MouseEvent event)
	{
		final ClientPadController padController = padView.getPadController();
		if(padController.getStatus() != null && padController.getStatus().isAnyPlayingState())
		{
			event.consume();
			return;
		}

		final Node rootNode = padView.getRootNode();
		final Dragboard dragboard = rootNode.startDragAndDrop(TransferMode.MOVE);

		// Create Snapshot
		final SnapshotParameters parameters = new SnapshotParameters();
		parameters.setFill(Color.TRANSPARENT);
		final WritableImage snapshot = rootNode.snapshot(parameters, null);
		for(int x = 0; x < snapshot.getWidth(); x++)
		{
			for(int y = 0; y < snapshot.getHeight(); y++)
			{
				Color oldColor = snapshot.getPixelReader().getColor(x, y).darker().darker();
				Color newColor = new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), oldColor.getOpacity() * 0.5);
				snapshot.getPixelWriter().setColor(x, y, newColor);
			}
		}
		dragboard.setDragView(snapshot);

		final ClipboardContent content = new ClipboardContent();
		content.put(DATA_FORMAT, padView.getCurrentPadIndex());
		dragboard.setContent(content);

		event.consume();
	}

	@Override
	public void onDragExited(DesktopPadView padView, DragEvent event)
	{
		if(event.getDragboard().hasContent(DATA_FORMAT))
		{
			padView.getDropOptionSelect().hide();
			event.consume();
		}
	}

	@Override
	public void onDragOver(DesktopPadView padView, DragEvent event)
	{
		final ClientPadController padController = padView.getPadController();
		if(padController.getStatus() != null && padController.getStatus().isAnyPlayingState())
		{
			event.consume();
			return;
		}

		if(event.getDragboard().hasContent(DATA_FORMAT))
		{
			if(padView.getCurrentPadIndex().equals(event.getDragboard().getContent(DATA_FORMAT)))
			{
				return;
			}

			final List<DropOption> fileDragOptions = List.of(
					new PadDropDuplicateOption(projectController, client, errorAlertBuilder),
					new PadDropSwapOption(projectController, client, errorAlertBuilder),
					new PadDropMoveOption(projectController, client, errorAlertBuilder)
			);

			padView.getDropOptionSelect().showOptions(fileDragOptions);

			event.acceptTransferModes(TransferMode.MOVE);
			event.consume();
		}
	}

	@Override
	public void onDragDropped(DesktopPadView padView, DragEvent event)
	{
		final ClientPadController padController = padView.getPadController();
		if(padController.getStatus() != null && padController.getStatus().isAnyPlayingState())
		{
			event.consume();
			return;
		}

		final Dragboard dragboard = event.getDragboard();
		if(dragboard.hasContent(DATA_FORMAT))
		{
			final DropOption dropOption = padView.getDropOptionSelect().getSelectedOption();
			if(dropOption == null)
			{
				return;
			}
			dropOption.handleDrag(padView, event);
			event.setDropCompleted(true);
			event.consume();
		}
	}

	@Override
	public void onDragOver(Page page, DragEvent event)
	{
		final Dragboard dragboard = event.getDragboard();
		if(dragboard.hasContent(DATA_FORMAT))
		{
			mainViewController.showPage(page);
		}
	}
}
