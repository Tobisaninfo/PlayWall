package de.tobias.playwall.client.domain.pad.view.control;

import de.tobias.playwall.client.domain.pad.PadIndex;
import de.tobias.playwall.client.domain.pad.view.PadIndexable;
import de.tobias.playwall.client.domain.pad.view.StyleIndexListener;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

import java.util.LinkedList;
import java.util.List;

public class PadHBox extends HBox implements PadIndexable
{
	private final ObjectProperty<PadIndex> indexProperty;
	private final List<StyleIndexListener> styleListeners = new LinkedList<>();

	public static PadHBox deepStyled(String... styleClasses)
	{
		PadHBox padHBox = new PadHBox(styleClasses);

		padHBox.getChildren().addListener((InvalidationListener) observable -> {
			for(StyleIndexListener listener : padHBox.styleListeners)
			{
				padHBox.indexProperty.removeListener(listener);
			}

			padHBox.styleListeners.clear();

			for(Node child : padHBox.getChildren())
			{
				StyleIndexListener listener = new StyleIndexListener(child, styleClasses);
				padHBox.indexProperty.addListener(listener);
				padHBox.styleListeners.add(listener);

				listener.changed(padHBox.indexProperty, null, padHBox.indexProperty.get());
			}
		});

		return padHBox;
	}

	public PadHBox(String... styleClasses)
	{
		this(0, styleClasses);
	}

	public PadHBox(double spacing, String... styleClasses)
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
