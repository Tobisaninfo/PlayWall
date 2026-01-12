package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.viewcontroller.AbstractViewControllerTest;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MainViewControllerProjectSettingsTest extends AbstractViewControllerTest
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
	}

	private void showMainView()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.openProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();
	}

	@Test
	void testProjectSettingsChangeName(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		robot.clickOn(robot.lookup(".menu").lookup("Datei").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Projekteinstellungen").queryLabeled());

		WaitForAsyncUtils.waitForFxEvents();

		final List<Window> windows = new ArrayList<>(robot.listWindows());
		final Stage stageSettings = (Stage) windows.getLast();

		assertThat(stageSettings.getTitle()).isEqualTo("Projekteinstellungen - Project 1");

		robot.lookup("#textFieldName").queryAs(TextField.class).setText("Fancy project name");
		robot.clickOn("#saveButton");

		WaitForAsyncUtils.waitForFxEvents();

		verify(client).updateProjectSettings(ProjectMetadata.builder()
				.name("Fancy project name")
				.id(UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c"))
				.numberOfHorizontalPads(4)
				.numberOfVerticalPads(4)
				.build());

		assertThat(mainViewController.getProjectTitleLabel().getText()).isEqualTo("Fancy project name");
	}
}
