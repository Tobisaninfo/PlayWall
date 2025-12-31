package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.common.api.project.model.PadControllerStatus;
import javafx.scene.Node;

public interface PadView
{
	Node getRootNode();

	Pad getPad();

	void updateFromPad(Pad pad);

	void showLoading(boolean isLoading);

	void updateStatus(PadControllerStatus status);
}
