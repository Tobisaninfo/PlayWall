package de.tobias.playwall.client.viewcontroller.style;

import de.tobias.playwall.client.model.project.Page;
import javafx.stage.Stage;

public interface Styleable
{
	void applyToStage(Stage stage);

	void renderStylesheets(Stage stage, Page page);
}
