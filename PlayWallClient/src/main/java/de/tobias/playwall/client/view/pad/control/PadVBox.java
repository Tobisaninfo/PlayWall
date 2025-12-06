package de.tobias.playwall.client.view.pad.control;

import de.tobias.playwall.client.model.project.PadIndex;
import de.tobias.playwall.client.view.pad.PadIndexable;
import de.tobias.playwall.client.view.pad.StyleIndexListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.VBox;

public class PadVBox extends VBox implements PadIndexable
{
	private final ObjectProperty<PadIndex> indexProperty;

	public PadVBox(String... styleClasses)
	{
		this(0, styleClasses);
	}

	public PadVBox(double spacing, String... styleClasses)
	{
		super(spacing);
		indexProperty = new SimpleObjectProperty<>();
		indexProperty.addListener(new StyleIndexListener(this, styleClasses));
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
