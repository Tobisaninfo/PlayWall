package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;

public class GlobalColorPicker extends ToggleButton
{
	private static final int PREVIEW_BOX_SIZE = 15;
	private static final ModernColor START_COLOR = ModernColor.BLUE1;

	private final ColorButton colorButton;
	private final ColorPicker colorPicker;

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
			// TODO: colorize pad on click
		});
	}
}
