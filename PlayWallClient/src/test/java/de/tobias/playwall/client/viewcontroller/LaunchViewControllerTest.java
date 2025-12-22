package de.tobias.playwall.client.viewcontroller;

import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.viewcontroller.dialog.ProjectNewDialog;
import de.tobias.playwall.client.viewcontroller.main.MainViewController;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.testfx.assertions.api.Assertions.assertThat;

class LaunchViewControllerTest extends AbstractViewControllerTest
{
	private static final UUID PROJECT_ID = UUID.randomUUID();
	private static final ProjectMetadata PROJECT_METADATA_1 = new ProjectMetadata(PROJECT_ID, "Test 1", 6, 4);

	private AppContext context;
	private final Client client = mock(Client.class);

	private final MainViewController mainViewController = mock(MainViewController.class);
	private final ProjectNewDialog projectNewDialog = mock(ProjectNewDialog.class);

	private LaunchDialog launchDialog;
	private Stage stage;

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazySingleton(MainViewController.class, _ -> mainViewController);
		context.registerLazySingleton(ProjectNewDialog.class, _ -> projectNewDialog);
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);
	}

	// List projects

	@Test
	void testProjectListDisplayAllProjects() throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(List.of(new ProjectMetadata(PROJECT_ID, "Test 1", 6, 4)));

		Platform.runLater(() -> {
			launchDialog = context.get(LaunchDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(launchDialog.getProjectListView()).hasExactlyNumItems(1);
	}

	@Test
	void testProjectListDisplayPlaceholder() throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(List.of());

		Platform.runLater(() -> {
			launchDialog = context.get(LaunchDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(launchDialog.getProjectListView()).hasExactlyNumItems(0);
	}

	// New project

	@Test
	void testNewProjectDialogOkay(FxRobot robot) throws PlayWallApiException
	{
		ProjectMetadata metadata = new ProjectMetadata(PROJECT_METADATA_1.id(), "Test 1", 4, 4);
		when(projectNewDialog.showAndWait(any())).thenReturn(Optional.of(metadata));

		Platform.runLater(() -> {
			launchDialog = context.get(LaunchDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();
		reset(client);

		when(client.launchProject(PROJECT_METADATA_1.id())).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		robot.clickOn(launchDialog.getNewProjectButton());
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).getProjects();

		// open project
		verify(client).launchProject(PROJECT_METADATA_1.id());
		verify(mainViewController).showStage();
		verify(mainViewController).openProject(any());
		assertThat(stage.isShowing()).isFalse();
	}

	@Test
	void testNewProjectDialogCanceled(FxRobot robot) throws PlayWallApiException
	{
		when(projectNewDialog.showAndWait(any())).thenReturn(Optional.empty());

		Platform.runLater(() -> {
			launchDialog = context.get(LaunchDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();
		reset(client);

		robot.clickOn(launchDialog.getNewProjectButton());
		WaitForAsyncUtils.waitForFxEvents();

		verify(client, never()).getProjects();

		// not open anything
		verify(client, never()).launchProject(any());
	}

	// Open

	@Test
	void testOpenProjectDoubleClick(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(List.of(PROJECT_METADATA_1));
		when(client.launchProject(PROJECT_METADATA_1.id())).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		Platform.runLater(() -> {
			launchDialog = context.get(LaunchDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final Node cell = launchDialog.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		robot.doubleClickOn(cell, Motion.DEFAULT);
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).launchProject(PROJECT_ID);
		verify(mainViewController).showStage();
		verify(mainViewController).openProject(any());
		assertThat(stage.isShowing()).isFalse();
	}

	@Test
	void testOpenProjectButtonClick(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(List.of(PROJECT_METADATA_1));
		when(client.launchProject(PROJECT_METADATA_1.id())).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		Platform.runLater(() -> {
			launchDialog = context.get(LaunchDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final Node cell = launchDialog.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		robot.clickOn(cell, Motion.DEFAULT);
		robot.clickOn(launchDialog.getOpenButton());
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).launchProject(PROJECT_ID);
		verify(mainViewController).showStage();
		verify(mainViewController).openProject(any());
		assertThat(stage.isShowing()).isFalse();
	}

	@Test
	void testOpenProjectError(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(List.of(PROJECT_METADATA_1));
		when(client.launchProject(PROJECT_METADATA_1.id())).thenThrow(new PlayWallApiException("Fehler beim öffnen des Projekts", null));

		Platform.runLater(() -> {
			launchDialog = context.get(LaunchDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final Node cell = launchDialog.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		robot.clickOn(cell, Motion.DEFAULT);
		robot.clickOn(launchDialog.getOpenButton());
		WaitForAsyncUtils.waitForFxEvents();

		verify(mainViewController, never()).showStage();
		assertThat(stage.isShowing()).isTrue();

		assertThat(robot.lookup(".label.content").queryLabeled()).hasText("Fehler beim öffnen des Projekts");
	}

	// Delete

	@Test
	void testProjectDeleteOkay(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(List.of(PROJECT_METADATA_1));

		Platform.runLater(() -> {
			launchDialog = context.get(LaunchDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final Node cell = launchDialog.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		robot.clickOn(cell, Motion.DEFAULT);
		robot.clickOn(launchDialog.getDeleteButton());
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn(robot.lookup("OK").lookup(".button").queryButton());
		verify(client).deleteProject(PROJECT_ID);
	}

	@Test
	void testProjectDeleteCancel(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(List.of(PROJECT_METADATA_1));

		Platform.runLater(() -> {
			launchDialog = context.get(LaunchDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final Node cell = launchDialog.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		robot.clickOn(cell, Motion.DEFAULT);
		robot.clickOn(launchDialog.getDeleteButton());
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn(robot.lookup("Abbrechen").lookup(".button").queryButton());
		verify(client, never()).deleteProject(PROJECT_ID);
	}

	@Test
	void testProjectDeleteError(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(List.of(PROJECT_METADATA_1));

		Platform.runLater(() -> {
			launchDialog = context.get(LaunchDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		reset(client);
		doThrow(new PlayWallApiException("Projekt kann nicht gelöscht werden", null)).when(client).deleteProject(PROJECT_ID);

		final Node cell = launchDialog.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		robot.clickOn(cell, Motion.DEFAULT);
		robot.clickOn(launchDialog.getDeleteButton());
		robot.clickOn(robot.lookup("OK").lookup(".button").queryButton());
		verify(client).deleteProject(PROJECT_ID);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(robot.lookup(".label.content").queryLabeled()).hasText("Projekt kann nicht gelöscht werden");
		verify(client, never()).getProjects();
	}
}
