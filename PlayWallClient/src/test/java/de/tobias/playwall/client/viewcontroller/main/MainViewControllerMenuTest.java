package de.tobias.playwall.client.viewcontroller.main;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationInfo;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.viewcontroller.AbstractViewControllerTest;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.mockito.Mockito.*;

class MainViewControllerMenuTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private Project project;

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		project = loadProject("projects/project_1.json");

		final App app = mock(App.class);
		final ApplicationInfo appInfo = mock(ApplicationInfo.class);
		when(appInfo.getName()).thenReturn("PlayWall");
		when(appInfo.getVersion()).thenReturn("0.0.1");
		when(appInfo.getAuthor()).thenReturn("Max Mustermann");
		when(app.getInfo()).thenReturn(appInfo);
		context.registerLazySingleton(App.class, _ -> app);
	}

	@Test
	void testMenuAbout(FxRobot robot)
	{
		final AboutDialog dialog = mock(AboutDialog.class);
		AppContextHolder.getInstance().registerLazySingleton(AboutDialog.class, _ -> dialog);

		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.openProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn(robot.lookup(".menu").lookup("Info").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Über PlayWall").queryLabeled());

		verify(dialog).showAndWait(stage);
	}
}
