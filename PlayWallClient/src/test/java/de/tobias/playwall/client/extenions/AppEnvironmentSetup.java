package de.tobias.playwall.client.extenions;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.PlayWallLocalizationDelegate;
import de.tobias.playwall.client.PlayWallMain;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.loader.AppContextLoader;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AppEnvironmentSetup implements BeforeEachCallback
{
	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception
	{
		ApplicationUtils.registerMainApplication(PlayWallMain.class);
		Localization.setDelegate(new PlayWallLocalizationDelegate());
		Localization.load();

		final AppContext context = new AppContext();
		AppContextLoader.setupDependencies(
				context,
				new String[]{PlayWallMain.class.getPackage().getName()},
				new String[]{"de.tobias.playwall.client.appcontext.loader.test"}
		);
		AppContextHolder.setInstance(context);
	}
}
