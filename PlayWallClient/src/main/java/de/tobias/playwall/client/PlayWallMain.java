package de.tobias.playwall.client;

import de.thecodelabs.logger.FileOutputOption;
import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.SystemUtils;
import de.tobias.playwall.client.viewcontroller.LaunchDialog;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class PlayWallMain extends Application
{
	private static final String ICON_PATH = "de/tobias/playwall/client/logo/icon_small.png";
	private Image stageIcon = null;


	public static void main(String[] args)
	{
		Localization.setDelegate(new PlayPadLocalizationDelegate());
		Localization.load();

		ApplicationUtils.addAppListener(PlayWallMain::applicationWillStart);
		App app = ApplicationUtils.registerMainApplication(PlayWallMain.class);

		app.start(args);
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


	@Override
	public void init()
	{
		Logger.info("Running on Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
		Logger.info("Run Path: {0}", SystemUtils.getRunPath());

		stageIcon = new Image(ICON_PATH);
		Alerts.getInstance().setDefaultIcon(stageIcon);
	}

	@Override
	public void start(Stage stage)
	{
		stage.getIcons().add(stageIcon);
		new LaunchDialog(stage);
	}
}