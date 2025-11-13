package de.tobias.playwall.client.extenions;

import de.thecodelabs.logger.FileOutputOption;
import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.PlayWallLocalizationDelegate;
import de.tobias.playwall.client.PlayWallMain;
import de.tobias.playwall.client.di.AppContext;
import de.tobias.playwall.client.di.AppContextHolder;
import de.tobias.playwall.client.di.loader.AppContextLoader;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Paths;

public class AppEnvironmentSetup implements BeforeEachCallback
{
	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception
	{
		ApplicationUtils.registerMainApplication(PlayWallMain.class);
		Logger.init(Paths.get("."));
		Logger.setLevelFilter(LogLevelFilter.DEBUG);
		Logger.setFileOutput(FileOutputOption.DISABLED);
		Localization.setDelegate(new PlayWallLocalizationDelegate());
		Localization.load();

		final AppContext context = new AppContext();
		AppContextLoader.setupDependencies(context);
		AppContextHolder.setInstance(context);
	}
}
