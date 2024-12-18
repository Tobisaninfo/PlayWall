package de.tobias.playwall.client.viewcontroller.style;

import javafx.stage.Stage;

public class ModernStyle implements Styleable
{
	@Override
	public void applyToStage(Stage stage)
	{
		stage.getScene().getStylesheets().add("style/style.css");
		stage.getScene().getStylesheets().add("style/modern.css");
	}
}
