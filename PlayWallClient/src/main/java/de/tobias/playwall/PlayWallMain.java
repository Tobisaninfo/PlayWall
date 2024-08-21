package de.tobias.playwall;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.viewcontroller.LaunchDialog;
import javafx.application.Application;
import javafx.stage.Stage;

public class PlayWallMain extends Application
{
	@Override
	public void start(Stage stage)
	{
		Localization.setDelegate(new PlayPadLocalizationDelegate());
		Localization.load();
		new LaunchDialog(stage);
	}

	public static void main(String[] args)
	{
		launch();
	}
}