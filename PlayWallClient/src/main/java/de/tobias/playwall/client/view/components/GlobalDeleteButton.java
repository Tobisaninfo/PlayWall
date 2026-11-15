package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.pad.view.desktop.PadEventDispatcher;
import de.tobias.playwall.client.domain.pad.view.desktop.listener.GlobalDeleteListener;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class GlobalDeleteButton extends ToggleButton
{
	private static final double CURSOR_SIZE_PX = 24.0;
	private static final Image CURSOR_IMAGE = new Image(
			"de/tobias/playwall/client/icon/trash-solid-full.png",
			CURSOR_SIZE_PX,
			CURSOR_SIZE_PX,
			true,
			true
	);

	private final ImageCursor imageCursor;

	private PadEventDispatcher eventDispatcher;
	private GlobalDeleteListener listener;
	private Node nodeThatShouldUseImageCursor;

	public GlobalDeleteButton()
	{
		final FontIcon icon = new FontIcon(FontAwesomeType.TRASH_CAN_SOLID);
		icon.setSize(14);
		icon.setMouseTransparent(true);

		final HBox box = new HBox(icon);
		box.setSpacing(ViewConstants.DEFAULT_SPACING / 2);
		box.setAlignment(Pos.CENTER);

		this.setGraphic(box);
		this.setFocusTraversable(false);

		this.setSelected(false);

		imageCursor = new ImageCursor(CURSOR_IMAGE, 0, 0);

		this.selectedProperty().addListener((_, _, newValue) -> onSelected(newValue));

		final Tooltip tooltip = new Tooltip();
		tooltip.setText(Localization.getString(Strings.UI_GLOBAL_DELETE_MODE_TOOLTIP));
		tooltip.setShowDelay(Duration.millis(300));
		setTooltip(tooltip);
	}

	private void onSelected(boolean newValue)
	{
		if(newValue)
		{
			nodeThatShouldUseImageCursor.setCursor(imageCursor);
			getStyleClass().add("danger");
			eventDispatcher.addFirstPadInputListener(listener);
		}
		else
		{
			if(nodeThatShouldUseImageCursor.getCursor() == imageCursor)
			{
				nodeThatShouldUseImageCursor.setCursor(Cursor.DEFAULT);
			}

			getStyleClass().remove("danger");
			eventDispatcher.removePadInputListener(listener);
		}
	}

	public void init(PadEventDispatcher eventDispatcher, Node nodeThatShouldUseImageCursor, GlobalDeleteListener listener)
	{
		this.eventDispatcher = eventDispatcher;
		this.nodeThatShouldUseImageCursor = nodeThatShouldUseImageCursor;
		this.listener = listener;
	}
}
