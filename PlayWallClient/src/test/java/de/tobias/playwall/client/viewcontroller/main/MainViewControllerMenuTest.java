package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.service.ClientProjectController;
import de.tobias.playwall.client.viewcontroller.AbstractViewControllerTest;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testfx.assertions.api.Assertions.assertThat;

class MainViewControllerMenuTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private Project project;

	private final Client client = mock(Client.class);

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);

		project = loadProject("projects/project_1.json");
		ClientProjectController projectController = context.get(ClientProjectController.class);
		projectController.loadProject(project);
	}

	private void showMainView()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();
	}

	@Test
	void testMenuSave(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		robot.clickOn(robot.lookup(".menu").lookup("Datei").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Projekt speichern").queryLabeled());

		verify(client).saveProject();
		assertThat(robot.lookup(".notification-bar").lookup(".label").queryLabeled()).hasText("Projekt gespeichert");
	}

	@Test
	@Disabled("Not working in headless mode")
	void testMenuSaveKeyboardShortcut(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		robot.push(KeyCode.SHORTCUT, KeyCode.S);

		verify(client).saveProject();
		assertThat(robot.lookup(".notification-bar").lookup(".label").queryLabeled()).hasText("Projekt gespeichert");
	}

	@Test
	void testMenuAbout(FxRobot robot)
	{
		final AboutDialog dialog = mock(AboutDialog.class);
		AppContextHolder.getInstance().registerLazySingleton(AboutDialog.class, _ -> dialog);

		showMainView();

		robot.clickOn(robot.lookup(".menu").lookup("Info").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Über PlayWall").queryLabeled());

		verify(dialog).showAndWait(stage);
	}
}
