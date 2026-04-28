package de.tobias.playwall.client;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.logger.LoggerBridge;
import de.thecodelabs.utils.logger.Slf4JBridge;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.SystemUtils;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.loader.AppContextLoader;
import de.tobias.playwall.client.log.LogServer;
import de.tobias.playwall.client.log.LogStore;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.view.launch.ApplicationLoadingViewController;
import de.tobias.playwall.client.view.style.AppIconProvider;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class PlayWallMain extends Application
{
	static
	{
		Locale.setDefault(Locale.GERMAN);
	}

	private static Logger log;

	public static void main(String[] args)
	{
		Thread.setDefaultUncaughtExceptionHandler((_, e) -> log.error("Uncaught error in thread execution", e));
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

		LoggerBridge.setImplementation(new Slf4JBridge());
		log = LoggerFactory.getLogger(PlayWallMain.class);
		log.info("Logging initialized (Running in LogLevel: {})", level);
	}

	@Override
	@SuppressWarnings({"java:S2629", "java:S2139"})
	public void init()
	{
		try
		{
			final AppContext appContext = new AppContext(AppContext.Environment.PRODUCTION);
			appContext.registerLazySingleton(App.class, _ -> ApplicationUtils.getApplication());
			AppContextLoader.setupDependencies(appContext);
			AppContextHolder.setInstance(appContext);

			startLogServer(appContext);

			log.info("Running on Java: {} ({})", System.getProperty("java.version"), System.getProperty("java.vendor"));
			log.info("Run Path: {}", SystemUtils.getRunPath());

			loadAppIcon();

			AppContextHolder.getInstance().get(FontLoader.class);
		}
		catch(Exception e)
		{
			log.error("Error initializing app", e);
			throw e;
		}
	}

	private static void startLogServer(AppContext appContext)
	{
		try
		{
			final LogServer logServer = new LogServer(LogServer.DEFAULT_PORT, appContext.get(LogStore.class)::onEntry);
			appContext.registerLazySingleton(LogServer.class, _ -> logServer);
			logServer.start();
		}
		catch(Exception e)
		{
			log.error("Error starting log server", e);
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
			log.error("Error starting app", e);
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
			log.error("Error stopping app", e);
		}
	}

	private void loadAppIcon()
	{
		final AppIconProvider iconProvider = AppContextHolder.getInstance().get(AppIconProvider.class);
		Alerts.getInstance().setDefaultIcon(iconProvider.getStageIcon());
	}
}