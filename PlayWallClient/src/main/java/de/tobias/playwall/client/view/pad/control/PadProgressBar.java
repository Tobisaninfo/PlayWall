package de.tobias.playwall.client.view.pad.control;

import de.tobias.playwall.client.model.project.PadIndex;
import de.tobias.playwall.client.view.pad.PadIndexable;
import de.tobias.playwall.client.view.pad.StyleIndexListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ProgressBar;

public class PadProgressBar extends ProgressBar implements PadIndexable
{
	private final ObjectProperty<PadIndex> indexProperty;

	public PadProgressBar(double progress, String... styleClasses) {
		super(progress);
		indexProperty = new SimpleObjectProperty<>();
		indexProperty.addListener(new StyleIndexListener(this, styleClasses));
	}

	public PadIndex getIndex() {
		return indexProperty.get();
	}

	public void setIndex(PadIndex index) {
		indexProperty.set(index);
	}
}
