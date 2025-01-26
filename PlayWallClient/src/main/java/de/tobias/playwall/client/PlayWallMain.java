package de.tobias.playwall.client;

import de.thecodelabs.logger.FileOutputOption;
import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.SystemUtils;
import de.tobias.playwall.client.di.Component;
import de.tobias.playwall.client.di.DI;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.viewcontroller.LaunchDialog;
import io.github.classgraph.*;
import javafx.application.Application;
import javafx.stage.Stage;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.function.Function;


public class PlayWallMain extends Application
{
	private Client client;

	public static void main(String[] args)
	{
		setupDependencies();

		Localization.setDelegate(new PlayWallLocalizationDelegate());
		Localization.load();

		ApplicationUtils.addAppListener(PlayWallMain::applicationWillStart);
		App app = ApplicationUtils.registerMainApplication(PlayWallMain.class);

		app.start(args);
	}

	@SuppressWarnings({"java:S112", "java:S3740", "unchecked", "rawtypes"})
	private static void setupDependencies()
	{
		final String basePackage = PlayWallMain.class.getPackage().getName();
		final String componentAnnotation = Component.class.getName();

		try(ScanResult scanResult = new ClassGraph()
				.verbose()
				.enableAllInfo()
				.acceptPackages(basePackage)
				.scan())
		{
			for(ClassInfo componentClassInfo : scanResult.getClassesWithAnnotation(componentAnnotation))
			{
				final Class<?> loadedClass = componentClassInfo.loadClass();

				final AnnotationInfo annotationInfo = componentClassInfo.getAnnotationInfo(componentAnnotation);
				final AnnotationParameterValueList annotationValues = annotationInfo.getParameterValues();

				Class superclass = ((AnnotationClassRef) annotationValues.get("superclass").getValue()).loadClass();
				if(superclass.equals(Object.class))
				{
					superclass = loadedClass;
				}

				final Function<DI, ?> loadFunction = di -> {
					try
					{
						return loadedClass.getConstructor().newInstance();
					}
					catch(NoSuchMethodException | InstantiationException | IllegalAccessException |
						  InvocationTargetException e)
					{
						Logger.error(MessageFormat.format("Cannot register component {0}", loadedClass), e);
						throw new RuntimeException(e);
					}
				};

				boolean isSingleton = (boolean) annotationValues.get("singleton").getValue();
				if(isSingleton)
				{
					DI.instance().registerLazySingleton(superclass, loadFunction);
				}
				else
				{
					DI.instance().registerLazy(superclass, loadFunction);
				}
			}
		}
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

		client = DI.instance().get(Client.class);
		client.connectWithRetries(10);

		loadAppIcon();
	}

	@Override
	public void start(Stage stage)
	{
		stage.getIcons().add(DI.instance().get(AppIconProvider.class).getStageIcon());
		new LaunchDialog(stage, client);
	}

	@Override
	public void stop()
	{
		client.disconnect();
		Worker.shutdown();
	}

	private void loadAppIcon() {
		final AppIconProvider iconProvider = DI.instance().get(AppIconProvider.class);
		Alerts.getInstance().setDefaultIcon(iconProvider.getStageIcon());
	}
}