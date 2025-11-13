package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.logger.FileOutputOption;
import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.PlayWallLocalizationDelegate;
import de.tobias.playwall.client.PlayWallMain;
import de.tobias.playwall.client.di.DI;
import de.tobias.playwall.client.di.loader.DiLoader;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.utils.ScreenshotOnFailure;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
@ExtendWith(ScreenshotOnFailure.class)
class LaunchViewControllerTest
{
	private Client client;
	private LaunchDialog launchDialog;

	private static final UUID PROJECT_ID = UUID.randomUUID();

	@Start
	private void start(Stage stage) throws Exception
	{
		ApplicationUtils.registerMainApplication(PlayWallMain.class);
		Logger.init(Paths.get("."));
		Logger.setLevelFilter(LogLevelFilter.DEBUG);
		Logger.setFileOutput(FileOutputOption.DISABLED);
		Localization.setDelegate(new PlayWallLocalizationDelegate());
		Localization.load();
		DiLoader.setupDependencies();

		client = mock(Client.class);
		when(client.getProjects()).thenReturn(List.of(new ProjectMetadata(PROJECT_ID, "Test 1", 6, 4)));
		DI.instance().registerLazySingleton(Client.class, _ -> client);

		launchDialog = DI.instance().get(LaunchDialog.class);
		launchDialog.applyViewControllerToStage(stage);
	}

	@Test
	void testProjectListDisplayAllProjects(FxRobot robot)
	{
		assertThat(launchDialog.getProjectListView()).hasExactlyNumItems(1);
	}

	@Test
	void testProjectDelete(FxRobot robot) throws Exception
	{
		final Node cell = launchDialog.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		robot.clickOn(cell, Motion.DEFAULT);
		robot.clickOn(launchDialog.getDeleteButton());
		robot.clickOn(robot.lookup("OK").lookup(".button").queryButton());
		verify(client).deleteProject(PROJECT_ID);
	}
}
