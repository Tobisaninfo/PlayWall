package de.tobias.playwall.client.extensions;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationInfo;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.PlayWallLocalizationDelegate;
import de.tobias.playwall.client.PlayWallMain;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.appcontext.loader.AppContextLoader;
import de.tobias.playwall.client.appcontext.loader.AppContextLoaderRequest;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.mockito.Mockito.*;

public class AppEnvironmentSetup implements BeforeEachCallback
{
	@Override
	public void beforeEach(ExtensionContext extensionContext)
	{
		final App app = ApplicationUtils.registerMainApplication(PlayWallMain.class);
		Localization.setDelegate(new PlayWallLocalizationDelegate());
		Localization.load();

		final AppContext context = new AppContext(AppContext.Environment.GUI_TESTING);
		AppContextLoader.setupDependencies(
				new AppContextLoaderRequest()
						.withAppContext(context)
						.withComponentInitializer(TestComponentInitializer::new)
						.withBasePackages(PlayWallMain.class.getPackage().getName())
						.withRejectPackages("de.tobias.playwall.client.appcontext.loader.test")
		);

		final App appSpy = spy(app);
		final ApplicationInfo appInfo = mock(ApplicationInfo.class);
		when(appInfo.getName()).thenReturn("PlayWall");
		when(appInfo.getVersion()).thenReturn("0.0.1");
		when(appInfo.getAuthor()).thenReturn("TheCodeLabs");
		when(appSpy.getInfo()).thenReturn(appInfo);
		context.registerLazySingleton(App.class, _ -> appSpy);

		AppContextHolder.setInstance(context);
	}
}
