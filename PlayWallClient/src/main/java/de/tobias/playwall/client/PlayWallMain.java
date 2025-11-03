package de.tobias.playwall.client;

import de.thecodelabs.logger.FileOutputOption;
import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.io.IOUtils;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


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

		try
		{
			prepareServerEnvironment();
		}
		catch(IOException e)
		{
			Logger.error(e);
			throw new RuntimeException(e);
		}

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

	private static void prepareServerEnvironment() throws IOException
	{
		final InputStream customJdkResource = PlayWallMain.class.getClassLoader().getResourceAsStream("server/custom-jdk.zip");
		final Path jdkHome = ApplicationUtils.getApplication().getPath(PathType.CACHE, "server", "jdk");
		if (customJdkResource != null) {
			Logger.info("Extracting custom JDK...");
			if (Files.notExists(jdkHome)) {
				Files.createDirectories(jdkHome);
			}
			unzip(customJdkResource, jdkHome);
		}

		final InputStream serverJarResource = PlayWallMain.class.getClassLoader().getResourceAsStream("server/PlayWallServer-8.0.0.jar");
		final Path serverJar = ApplicationUtils.getApplication().getPath(PathType.CACHE, "server", "PlayWallServer-8.0.0.jar");
		if (serverJarResource != null) {
			Logger.info("Extracting PlayWallServer...");
			IOUtils.copy(serverJarResource, serverJar);
		}

		ProcessBuilder processBuilder = new ProcessBuilder(jdkHome.toString() + "/bin/java", "-jar", serverJar.toString());
		processBuilder.directory(jdkHome.toFile());
		processBuilder.start();
	}

	private static void unzip(InputStream zipInputStream, Path targetDir) throws IOException
	{
		try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				Path newFile = targetDir.resolve(entry.getName()).normalize();

				// Sicherheitscheck: Verhindert Pfad-Traversal
				if (!newFile.startsWith(targetDir)) {
					throw new IOException("Ungültiger ZIP-Eintrag: " + entry.getName());
				}

				if (entry.isDirectory()) {
					Files.createDirectories(newFile);
				} else {
					Files.createDirectories(newFile.getParent());
					Logger.debug("Extracting file: " + newFile);
					try (OutputStream os = Files.newOutputStream(newFile)) {
						byte[] buffer = new byte[4096];
						int len;
						while ((len = zis.read(buffer)) > 0) {
							os.write(buffer, 0, len);
						}
					}
				}
				zis.closeEntry();
			}
		}
	}
}