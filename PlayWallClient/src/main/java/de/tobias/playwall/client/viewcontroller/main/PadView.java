package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.model.project.PadStatus;
import de.tobias.playwall.client.service.ClientPadController;
import javafx.scene.Node;
import javafx.util.Duration;

public interface PadView
{
	Node getRootNode();

	ClientPadController getPadController();

	void updateFromPad(int currentPage, ClientPadController controller);

	void showLoading(boolean isLoading);

	void updateStatus(PadStatus status);

	void updateTimeNodes();
}
