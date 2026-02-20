package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.view.PadView;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.function.BiConsumer;

public class GlobalColorPicker extends ToggleButton
{
	private static final int PREVIEW_BOX_SIZE = 15;
	private static final double CURSOR_SIZE_PX = 24.0;

	private static final ModernColor START_COLOR = ModernColor.BLUE1;

	private final ColorButton colorButton;
	private final ColorPicker colorPicker;

	private List<PadView> padViews;

	private BiConsumer<Pad, ModernColor> onColorChange;

	public GlobalColorPicker()
	{
		final FontIcon icon = new FontIcon(FontAwesomeType.PAINTBRUSH_SOLID);
		icon.setSize(14);
		icon.setMouseTransparent(true);

		colorButton = initColorButton();
		colorPicker = new ColorPicker(START_COLOR, ModernColor.values(), colorButton::updateColor);

		colorButton.setOnAction(_ -> {
			colorPicker.hide();
			colorPicker.show(colorButton);
			this.setSelected(true);
		});

		final HBox box = new HBox(icon, colorButton);
		box.setSpacing(ViewConstants.DEFAULT_SPACING / 2);

		this.setGraphic(box);
		this.setAlignment(Pos.CENTER_RIGHT);

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
					cursorImage.getWidth() / 2.0,
					cursorImage.getHeight() / 2.0
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
			getScene().setCursor(imageCursor);

			for(PadView padView : padViews)
			{
				addPadViewListeners(padView);
			}
		}
		else
		{
			getScene().setCursor(Cursor.DEFAULT);

			for(PadView padView : padViews)
			{
				padView.getRootNode().setOnMouseClicked(null);
				padView.getRootNode().setOnDragDetected(null);
				padView.getRootNode().setOnMouseDragEntered(null);
				padView.disableSettingsButton(false);
			}
		}
	}

	private void addPadViewListeners(PadView padView)
	{
		padView.getRootNode().setOnMouseClicked(event -> {
			if(event.getButton() == MouseButton.PRIMARY)
			{
				updatePadColor(padView);
			}
		});

		padView.getRootNode().setOnDragDetected(event -> {
			if(event.isPrimaryButtonDown())
			{
				padView.getRootNode().startFullDrag();
				updatePadColor(padView);
				event.consume();
			}
		});

		padView.getRootNode().setOnMouseDragEntered(event -> {
			if(event.isPrimaryButtonDown())
			{
				updatePadColor(padView);
			}
		});

		padView.disableSettingsButton(true);
	}

	private void updatePadColor(PadView padView)
	{
		final Pad pad = padView.getPadController().getPad();
		if(pad.getDefaultColor() == colorPicker.getSelectedColor())
		{
			return;
		}

		onColorChange.accept(pad, colorPicker.getSelectedColor());
	}

	public void init(List<PadView> padViews, BiConsumer<Pad, ModernColor> onColorChange)
	{
		this.padViews = padViews;
		this.onColorChange = onColorChange;
	}
}
