package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.PadStatus;
import javafx.scene.Node;
import javafx.util.Duration;

public interface PadView
{
	Node getRootNode();

	Pad getPad();

	void updateFromPad(int currentPage, Pad pad);

	void showLoading(boolean isLoading);

	void updateStatus(PadStatus status);

	void updatePlayPosition(Duration position);

	void setPadDuration(Duration padDuration);
}
