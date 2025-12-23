package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.model.project.Pad;
import javafx.scene.Node;

public interface PadView
{
	Node getRootNode();

	Pad getPad();

	void updateFromPad(Pad pad);

	void showLoading(boolean isLoading);
}
