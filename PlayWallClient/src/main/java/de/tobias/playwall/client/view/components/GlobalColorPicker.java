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
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.List;

public class GlobalColorPicker extends ToggleButton
{
	private static final int PREVIEW_BOX_SIZE = 15;
	private static final ModernColor START_COLOR = ModernColor.BLUE1;

	private final ColorButton colorButton;
	private final ColorPicker colorPicker;

	private List<PadView> padViews;
	private FluentClient client;

	public GlobalColorPicker()
	{
		final FontIcon icon = new FontIcon(FontAwesomeType.PALETTE_SOLID);
		icon.setSize(14);
		icon.setMouseTransparent(true);

		colorButton = new ColorButton();
		colorButton.setMinWidth(PREVIEW_BOX_SIZE);
		colorButton.setMinHeight(PREVIEW_BOX_SIZE);
		colorButton.setMaxWidth(PREVIEW_BOX_SIZE);
		colorButton.setMaxHeight(PREVIEW_BOX_SIZE);
		colorButton.updateColor(START_COLOR);
		colorButton.setGraphic(null);
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
			// TODO: disable pad and page buttons

			if(newValue)
			{
				getScene().setCursor(Cursor.HAND);

				for(PadView padView : padViews)
				{
					padView.getRootNode().setOnMouseClicked(event -> onPadClicked(padView, event));
				}
			}
			else
			{
				getScene().setCursor(Cursor.DEFAULT);

				for(PadView padView : padViews)
				{
					padView.getRootNode().setOnMouseClicked(null);
				}
			}
		});
	}

	private void onPadClicked(PadView padView, MouseEvent event)
	{
		if(event.getButton() != MouseButton.PRIMARY)
		{
			return;
		}

		final Pad pad = padView.getPadController().getPad();
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
