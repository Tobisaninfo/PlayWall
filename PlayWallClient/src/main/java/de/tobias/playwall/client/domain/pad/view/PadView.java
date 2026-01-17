package de.tobias.playwall.client.domain.pad.view;

import de.tobias.playwall.client.domain.pad.PadStatus;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import javafx.scene.Node;

public interface PadView
{
	Node getRootNode();

	ClientPadController getPadController();

	void updateFromPad(int currentPage, ClientPadController controller);

	void showLoading(boolean isLoading);

	void updateStatus(PadStatus status);

	void updateTimeNodes();
}
