package de.tobias.playwall.client;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.SystemUtils;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.loader.AppContextLoader;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.view.launch.ApplicationLoadingViewController;
import de.tobias.playwall.client.view.style.AppIconProvider;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.LoggerFactory;

public class PlayWallMain extends Application
{
	private static org.slf4j.Logger logger;

	public static void main(String[] args)
	{
		Thread.setDefaultUncaughtExceptionHandler((_, e) -> logger.error("Uncaught error in thread execution", e));
		Localization.setDelegate(new PlayWallLocalizationDelegate());
		Localization.load();

		ApplicationUtils.addAppListener(PlayWallMain::applicationWillStart);
		App app = ApplicationUtils.registerMainApplication(PlayWallMain.class);

		app.start(args);
	}

	private static void applicationWillStart(App app)
	{
		System.setProperty("app.logdir", app.getPath(PathType.LOG).toAbsolutePath().toString());
		System.setProperty("app.debug", String.valueOf(app.isDebug()));

		final Level level = app.isDebug() ? Level.DEBUG : Level.INFO;
		Configurator.setRootLevel(level);

		logger = LoggerFactory.getLogger(PlayWallMain.class);
		logger.info("Logging initialized (Running in LogLevel: {})", level);
	}

	@Override
	public void init()
	{
		try
		{
			final AppContext appContext = new AppContext(AppContext.Environment.PRODUCTION);
			appContext.registerLazySingleton(App.class, _ -> ApplicationUtils.getApplication());
			AppContextLoader.setupDependencies(appContext);
			AppContextHolder.setInstance(appContext);

			Logger.info("Running on Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
			Logger.info("Run Path: {0}", SystemUtils.getRunPath());

			loadAppIcon();

			AppContextHolder.getInstance().get(FontLoader.class);
		}
		catch(Exception e)
		{
			Logger.error(e);
			throw e;
		}
	}

	@Override
	public void start(Stage stage)
	{
		try
		{
			stage.getIcons().add(AppContextHolder.getInstance().get(AppIconProvider.class).getStageIcon());

			final ApplicationLoadingViewController viewController = AppContextHolder.getInstance().get(ApplicationLoadingViewController.class);
			viewController.applyViewControllerToStage(stage);
			viewController.showStage();
		}
		catch(Exception e)
		{
			Logger.error(e);
		}
	}

	@Override
	public void stop()
	{
		try
		{
			// Server gets stopped via "Runtime.getRuntime().addShutdownHook()"
			AppContextHolder.getInstance().get(Client.class).disconnect();
			Worker.shutdown();
		}
		catch(Exception e)
		{
			Logger.error(e);
		}
	}

	private void loadAppIcon()
	{
		final AppIconProvider iconProvider = AppContextHolder.getInstance().get(AppIconProvider.class);
		Alerts.getInstance().setDefaultIcon(iconProvider.getStageIcon());
	}
}