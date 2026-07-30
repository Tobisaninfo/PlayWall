package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.domain.pad.view.desktop.PadEventDispatcher;
import de.tobias.playwall.client.domain.pad.view.desktop.listener.GlobalPickerColorListener;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

public class GlobalColorPicker extends ToggleButton
{
	private static final int PREVIEW_BOX_SIZE = 15;
	private static final double CURSOR_SIZE_PX = 24.0;

	private static final ModernColor START_COLOR = ModernColor.BLUE1;

	private final ColorButton colorButton;
	private final ColorPicker colorPicker;

	private PadEventDispatcher eventDispatcher;
	private GlobalPickerColorListener listener;
	private Node nodeThatShouldUseImageCursor;

	public GlobalColorPicker()
	{
		final FontIcon icon = new FontIcon(FontAwesomeType.PAINTBRUSH_SOLID);
		icon.setSize(14);
		icon.setMouseTransparent(true);

		colorButton = initColorButton();
		colorButton.setFocusTraversable(false);
		colorPicker = new ColorPicker(START_COLOR, ModernColor.values(), colorButton::updateColor);

		colorButton.setOnAction(_ -> {
			colorPicker.hide();
			colorPicker.show(colorButton);
			this.setSelected(true);
		});

		final HBox box = new HBox(icon, colorButton);
		box.setSpacing(ViewConstants.DEFAULT_SPACING / 2);
		box.setAlignment(Pos.CENTER);

		this.setGraphic(box);
		this.setFocusTraversable(false);

		this.setSelected(false);

		this.selectedProperty().addListener((_, _, newValue) -> {
			final Image cursorImage = new Image(
					"de/tobias/playwall/client/icon/paintbrush-solid-full.png",
					CURSOR_SIZE_PX,
					CURSOR_SIZE_PX,
					true,
					true
			);
			final ImageCursor imageCursor = new ImageCursor(
					cursorImage,
					0,
					cursorImage.getHeight()
			);

			onSelected(newValue, imageCursor);
		});
	}

	private ColorButton initColorButton()
	{
		final ColorButton button = new ColorButton();

		button.setMinWidth(PREVIEW_BOX_SIZE);
		button.setMinHeight(PREVIEW_BOX_SIZE);
		button.setMaxWidth(PREVIEW_BOX_SIZE);
		button.setMaxHeight(PREVIEW_BOX_SIZE);
		button.updateColor(START_COLOR);
		button.setGraphic(null);

		return button;
	}

	private void onSelected(boolean newValue, ImageCursor imageCursor)
	{
		if(newValue)
		{
			nodeThatShouldUseImageCursor.setCursor(imageCursor);
			eventDispatcher.addFirstPadInputListener(listener);
		}
		else
		{
			nodeThatShouldUseImageCursor.setCursor(Cursor.DEFAULT);
			eventDispatcher.removePadInputListener(listener);
		}
	}

	public void init(PadEventDispatcher eventDispatcher,  Node nodeThatShouldUseImageCursor, GlobalPickerColorListener listener)
	{
		this.eventDispatcher = eventDispatcher;
		this.nodeThatShouldUseImageCursor = nodeThatShouldUseImageCursor;
		this.listener = listener;
	}

	public ModernColor getSelectedColor()
	{
		return colorPicker.getSelectedColor();
	}
}
