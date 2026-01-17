package de.tobias.playwall.client.view.style;

import de.tobias.playwall.client.domain.page.Page;
import javafx.stage.Stage;

public interface Styleable
{
	void applyToStage(Stage stage);

	void renderStylesheets(Stage stage, Page page);
}
