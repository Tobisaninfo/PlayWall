package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.paint.Color;

public class PadDragListener extends PadInputListener
{
	private static final String PAD_INDEX_DATATYPE = "de.tobias.playwall.pad_index";
	private static final DataFormat DATA_FORMAT = new DataFormat(PAD_INDEX_DATATYPE);

	@Override
	public void onDragDetected(DesktopPadView padView, MouseEvent event)
	{
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
}
