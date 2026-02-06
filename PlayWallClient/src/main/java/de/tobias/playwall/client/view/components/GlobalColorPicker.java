package de.tobias.playwall.client.view.components;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.view.PadView;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

import java.util.List;

public class GlobalColorPicker extends ToggleButton
{
	private static final int PREVIEW_BOX_SIZE = 15;
	private static final double CURSOR_SIZE_PX = 24.0;

	private static final ModernColor START_COLOR = ModernColor.BLUE1;

	private final ColorButton colorButton;
	private final ColorPicker colorPicker;

	private List<PadView> padViews;
	private FluentClient client;

	public GlobalColorPicker()
	{
		final FontIcon icon = new FontIcon(FontAwesomeType.PAINTBRUSH_SOLID);
		icon.setSize(14);
		icon.setMouseTransparent(true);

		colorButton = initColorButton();
		colorPicker = new ColorPicker(START_COLOR, ModernColor.values(), colorButton::updateColor);

		colorButton.setOnAction((_) -> {
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
		final ColorButton colorButton = new ColorButton();

		colorButton.setMinWidth(PREVIEW_BOX_SIZE);
		colorButton.setMinHeight(PREVIEW_BOX_SIZE);
		colorButton.setMaxWidth(PREVIEW_BOX_SIZE);
		colorButton.setMaxHeight(PREVIEW_BOX_SIZE);
		colorButton.updateColor(START_COLOR);
		colorButton.setGraphic(null);

		return colorButton;
	}

	private void onSelected(Boolean newValue, ImageCursor imageCursor)
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

		pad.setDefaultColor(colorPicker.getSelectedColor());

		try
		{
			client.pad(pad.getId()).updateSettings(pad);
		}
		catch(PlayWallApiException e)
		{
			Logger.error(e.getMessage());
			Alerts.getInstance().createAlert(Alert.AlertType.WARNING, null, e.getMessage(), getScene().getWindow()).showAndWait();
		}
	}

	public void init(List<PadView> padViews, FluentClient client)
	{
		this.padViews = padViews;
		this.client = client;
	}
}
