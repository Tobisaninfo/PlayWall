package de.tobias.playwall;

import de.thecodelabs.logger.FileOutputOption;
import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.viewcontroller.LaunchDialog;
import javafx.application.Application;
import javafx.stage.Stage;

public class PlayWallMain extends Application
{
	@Override
	public void start(Stage stage)
	{
		new LaunchDialog(stage);
	}

	private static void applicationWillStart(App app)
	{
		Logger.init(app.getPath(PathType.LOG));
		if(app.isDebug())
		{
			Logger.setLevelFilter(LogLevelFilter.DEBUG);
			Logger.setFileOutput(FileOutputOption.DISABLED);
			Logger.addFilter(message -> !message.getCaller().getClassName().contains("org.apache.commons.logging.impl.SLF4JLog"));
		}
		else
		{
			Logger.setFileOutput(FileOutputOption.COMBINED);
		}
		Logger.info("Logging initialized (Running in LogLevel: {0})", Logger.getLevelFilter().toString());
	}


	public static void main(String[] args)
	{
		Localization.setDelegate(new PlayPadLocalizationDelegate());
		Localization.load();

		ApplicationUtils.addAppListener(PlayWallMain::applicationWillStart);
		App app = ApplicationUtils.registerMainApplication(PlayWallMain.class);

		app.start(args);
	}
}