package de.tobias.playwall.client.viewcontroller.style;

import de.tobias.playwall.client.appcontext.Service;
import javafx.stage.Stage;

@Service(superclass = Styleable.class)
public class ModernStyle implements Styleable
{
	@Override
	public void applyToStage(Stage stage)
	{
		stage.getScene().getStylesheets().add("style/style.css");
		stage.getScene().getStylesheets().add("style/modern.css");
		stage.getScene().getStylesheets().add("style/settings.css");
	}
}
