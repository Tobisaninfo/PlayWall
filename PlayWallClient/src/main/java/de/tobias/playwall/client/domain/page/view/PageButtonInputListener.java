package de.tobias.playwall.client.domain.page.view;

import de.tobias.playwall.client.domain.page.Page;
import javafx.event.ActionEvent;
import javafx.scene.input.DragEvent;

public interface PageButtonInputListener
{
	default void onAction(Page page, ActionEvent event)
	{
	}

	default void onDragOver(Page page, DragEvent event)
	{
	}
}
