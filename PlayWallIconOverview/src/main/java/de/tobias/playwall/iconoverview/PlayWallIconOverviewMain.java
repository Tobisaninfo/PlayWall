package de.tobias.playwall.iconoverview;

import de.thecodelabs.logger.FileOutputOption;
import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.util.Localization;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;


public class PlayWallIconOverviewMain extends Application
{
	public static Image icon;
	public static App app;

	public static void main(String[] args)
	{
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> Logger.error(e));
		Localization.setDelegate(new PlayWallIconOverviewLocalizationDelegate());
		Localization.load();

		ApplicationUtils.addAppListener(PlayWallIconOverviewMain::applicationWillStart);
		app = ApplicationUtils.registerMainApplication(PlayWallIconOverviewMain.class);

		app.start(args);
	}

	private static void applicationWillStart(App app)
	{
		Logger.init(app.getPath(PathType.LOG), app.isDebug() ? FileOutputOption.DISABLED : FileOutputOption.COMBINED);
		if(app.isDebug())
		{
			Logger.setLevelFilter(LogLevelFilter.DEBUG);
			Logger.addFilter(message -> !message.getCaller().getClassName().contains("org.apache.commons.logging.impl.SLF4JLog"));
		}
		Logger.info("Logging initialized (Running in LogLevel: {0})", Logger.getLevelFilter().toString());
	}

	@Override
	public void init() throws Exception
	{
		icon = new Image("de/tobias/playwall/iconoverview/icon_small.png");

		final URL resource = this.getClass().getClassLoader().getResource("fonts/fontawesome-webfont.ttf");
		Font.loadFont(Objects.requireNonNull(resource).toExternalForm(), 16);
	}

	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			final PlayWallIconOverviewMainViewController controller = new PlayWallIconOverviewMainViewController(primaryStage, app);
			controller.showStage();
		}
		catch(Exception e)
		{
			Logger.error(e);
		}
	}
}