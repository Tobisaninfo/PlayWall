package de.tobias.playwall.client.domain.pad.view.control;

import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.domain.pad.PadIndex;
import de.tobias.playwall.client.domain.pad.view.PadIndexable;
import de.tobias.playwall.client.domain.pad.view.StyleIndexListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;

import static de.tobias.playwall.client.domain.pad.view.control.PadStyleClasses.STYLE_CLASS_PAD_ICON;
import static de.tobias.playwall.client.domain.pad.view.control.PadStyleClasses.STYLE_CLASS_PAD_ICON_INDEX;


public class PadLabel extends Label implements PadIndexable
{
	private final ObjectProperty<PadIndex> indexProperty;
	private StyleIndexListener graphicsListener;

	public static PadLabel empty(String... styleClasses)
	{
		return new PadLabel("", styleClasses);
	}

	public PadLabel(FontIcon icon, String... styleClasses)
	{
		this("", styleClasses);
		setGraphic(icon);
	}

	public PadLabel(String text, String... styleClasses)
	{
		super(text);

		indexProperty = new SimpleObjectProperty<>();
		indexProperty.addListener(new StyleIndexListener(this, styleClasses));
		initStyleGraphicsListener();

		graphicProperty().addListener(observable -> {
			if(graphicsListener != null)
			{
				indexProperty.removeListener(graphicsListener);
				graphicsListener = null;

			}
			initStyleGraphicsListener();
		});
	}

	private void initStyleGraphicsListener()
	{
		if(getGraphic() != null)
		{
			graphicsListener = new StyleIndexListener(getGraphic(), STYLE_CLASS_PAD_ICON, STYLE_CLASS_PAD_ICON_INDEX);
			indexProperty.addListener(graphicsListener);
		}
	}

	public PadIndex getIndex()
	{
		return indexProperty.get();
	}

	public void setIndex(PadIndex index)
	{
		indexProperty.set(index);
	}
}
