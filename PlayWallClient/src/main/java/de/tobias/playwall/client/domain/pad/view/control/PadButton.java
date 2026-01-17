package de.tobias.playwall.client.domain.pad.view.control;

import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.domain.pad.PadIndex;
import de.tobias.playwall.client.domain.pad.view.PadIndexable;
import de.tobias.playwall.client.domain.pad.view.StyleIndexListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

import static de.tobias.playwall.client.domain.pad.view.control.PadStyleClasses.*;

public class PadButton extends Button implements PadIndexable
{

	private final ObjectProperty<PadIndex> indexProperty;

	public PadButton(FontIcon icon, EventHandler<ActionEvent> onAction)
	{
		super("", icon);

		setFocusTraversable(false);
		setOnAction(onAction);

		indexProperty = new SimpleObjectProperty<>();
		indexProperty.addListener(new StyleIndexListener(this, STYLE_CLASS_PAD_BUTTON, STYLE_CLASS_PAD_BUTTON_INDEX));
		indexProperty.addListener(new StyleIndexListener(getGraphic(), STYLE_CLASS_PAD_ICON, STYLE_CLASS_PAD_ICON_INDEX));
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
