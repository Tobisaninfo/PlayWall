package de.tobias.playwall.client.view.components;

import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import org.controlsfx.control.PopOver;

import java.util.List;
import java.util.function.Consumer;

public class ColorPicker extends PopOver
{
	private Rectangle currentSelected;

	@Getter
	private ModernColor selectedColor;

	public ColorPicker(ModernColor startColor, ModernColor[] colors, Consumer<ModernColor> onSelectedCallback)
	{
		final GridPane gridPane = initContent(startColor, colors, onSelectedCallback);

		setContentNode(new VBox(gridPane));
		setDetachable(false);
		setCornerRadius(5);
		setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
	}

	private GridPane initContent(ModernColor startColor, ModernColor[] colors, Consumer<ModernColor> onSelectedCallback)
	{
		final GridPane gridPane = new GridPane();
		gridPane.setVgap(5);
		gridPane.setHgap(5);
		gridPane.setPadding(new Insets(5));

		double size = Math.sqrt(colors.length);
		int iSize = (int) size;
		if(size != iSize)
		{
			iSize++;
		}

		int index = 0;
		for(int y = 0; y < iSize; y++)
		{
			for(int x = 0; x < iSize; x++)
			{
				if(index < colors.length)
				{
					final ModernColor color = colors[index++];

					// Style in CSS
					final Rectangle rectangle = new Rectangle(40, 40);
					rectangle.setFill(color.getColor());
					rectangle.getStyleClass().add("color-view-item");

					// Gestrichelte Linie
					if(color.equals(startColor))
					{
						onSelect(rectangle, color);
					}

					// EventHandler
					rectangle.setOnMouseReleased(event ->
					{
						onSelect(rectangle, color);
						onSelectedCallback.accept(color);
						hide();
					});
					gridPane.add(rectangle, x, y);
				}
			}
		}

		return gridPane;
	}

	private void onSelect(Rectangle rectangle, ModernColor color)
	{
		if(currentSelected != null)
		{
			currentSelected.getStrokeDashArray().clear();
		}
		rectangle.getStrokeDashArray().addAll(3.0);
		currentSelected = rectangle;
		selectedColor = color;
	}
}
