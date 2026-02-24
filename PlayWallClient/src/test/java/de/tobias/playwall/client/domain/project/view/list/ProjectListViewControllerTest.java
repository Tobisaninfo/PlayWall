package de.tobias.playwall.client.domain.project.view.list;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.CommandLineOptions;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.AllProjectsInfo;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.project.view.ProjectNewDialog;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.domain.settings.view.settings.ProgramSettingsViewController;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
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

class ProjectListViewControllerTest extends AbstractViewControllerTest
{
	private static final UUID PROJECT_ID = UUID.randomUUID();
	private static final ProjectMetadata PROJECT_METADATA_1 = new ProjectMetadata(PROJECT_ID, "Test 1", 6, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3);

	private AppContext context;
	private final Client client = mock(Client.class);

	private final MainViewController mainViewController = mock(MainViewController.class);
	private final ProjectNewDialog projectNewDialog = mock(ProjectNewDialog.class);
	private final ProgramSettingsViewController programSettingsViewController = mock(ProgramSettingsViewController.class);
	private final CommandLineOptions commandLineOptions = mock(CommandLineOptions.class);
	private final ClientSettingsController settingsController = mock(ClientSettingsController.class);

	private ProjectListViewController launchDialog;
	private Stage stage;

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazySingleton(MainViewController.class, _ -> mainViewController);
		context.registerLazySingleton(ProjectNewDialog.class, _ -> projectNewDialog);
		context.registerLazySingleton(ProgramSettingsViewController.class, _ -> programSettingsViewController);
		context.registerLazySingleton(CommandLineOptions.class, _ -> commandLineOptions);
		context.registerLazySingleton(ClientSettingsController.class, _ -> settingsController);
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);

		when(settingsController.getSettings()).thenReturn(Settings.builder()
				.autoLoadLatestProjectOnStart(false)
				.build());
	}

	// List projects

	@Test
	void testProjectListDisplayAllProjects() throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(new ProjectMetadata(PROJECT_ID, "Test 1", 6, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3)))
				.recentProjectIds(List.of(PROJECT_ID))
				.build());

		Platform.runLater(() -> {
			launchDialog = context.get(ProjectListViewController.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(launchDialog.getProjectListView()).hasExactlyNumItems(1);
	}

	@Test
	void testProjectListDisplayPlaceholder() throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of())
				.recentProjectIds(List.of())
				.build());

		Platform.runLater(() -> {
			launchDialog = context.get(ProjectListViewController.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(launchDialog.getProjectListView()).hasExactlyNumItems(0);
	}

	// New project

	@Test
	void testNewProjectDialogOkay(FxRobot robot) throws PlayWallApiException
	{
		ProjectMetadata metadata = new ProjectMetadata(PROJECT_METADATA_1.getId(), "Test 1", 4, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3);
		when(projectNewDialog.showAndWait(any())).thenReturn(Optional.of(metadata));

		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(metadata))
				.recentProjectIds(List.of())
				.build());

		Platform.runLater(() -> {
			launchDialog = context.get(ProjectListViewController.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();
		reset(client);

		final Project project = new Project(PROJECT_METADATA_1, List.of());
		when(client.getProject(PROJECT_METADATA_1.getId())).thenReturn(project);

		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(metadata))
				.recentProjectIds(List.of())
				.build());

		robot.clickOn(launchDialog.getNewProjectButton());
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).getProjects();

		// open project
		verify(mainViewController).showStage();
		verify(mainViewController).showProject(project);
		assertThat(stage.isShowing()).isFalse();
	}

	@Test
	void testNewProjectDialogCanceled(FxRobot robot) throws PlayWallApiException
	{
		when(projectNewDialog.showAndWait(any())).thenReturn(Optional.empty());

		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of())
				.recentProjectIds(List.of())
				.build());

		Platform.runLater(() -> {
			launchDialog = context.get(ProjectListViewController.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();
		reset(client);

		robot.clickOn(launchDialog.getNewProjectButton());
		WaitForAsyncUtils.waitForFxEvents();

		verify(client, never()).getProjects();
	}

	// Open

	@Test
	void testOpenProjectDoubleClick(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());

		when(client.getProject(PROJECT_METADATA_1.getId())).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		Platform.runLater(() -> {
			launchDialog = context.get(ProjectListViewController.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final Node cell = launchDialog.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		robot.doubleClickOn(cell, Motion.DEFAULT);
		WaitForAsyncUtils.waitForFxEvents();

		verify(mainViewController).showStage();
		verify(mainViewController).showProject(any());
		assertThat(stage.isShowing()).isFalse();
	}

	@Test
	void testOpenProjectButtonClick(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());
		when(client.getProject(PROJECT_METADATA_1.getId())).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		Platform.runLater(() -> {
			launchDialog = context.get(ProjectListViewController.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final Node cell = launchDialog.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		robot.clickOn(cell, Motion.DEFAULT);
		robot.clickOn(launchDialog.getOpenButton());
		WaitForAsyncUtils.waitForFxEvents();

		verify(mainViewController).showStage();
		verify(mainViewController).showProject(any());
		assertThat(stage.isShowing()).isFalse();
	}

	@Test
	void testOpenProjectError(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());
		when(client.getProject(PROJECT_METADATA_1.getId())).thenThrow(new PlayWallApiException("Fehler beim öffnen des Projekts", null));

		Platform.runLater(() -> {
			launchDialog = context.get(ProjectListViewController.class);
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
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());

		Platform.runLater(() -> {
			launchDialog = context.get(ProjectListViewController.class);
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
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());

		Platform.runLater(() -> {
			launchDialog = context.get(ProjectListViewController.class);
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
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());

		Platform.runLater(() -> {
			launchDialog = context.get(ProjectListViewController.class);
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

	// Settings


	@Test
	void testOpenSettingsView(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(new ProjectMetadata(PROJECT_ID, "Test 1", 6, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3)))
				.recentProjectIds(List.of(PROJECT_ID))
				.build());

		Platform.runLater(() -> {
			launchDialog = context.get(ProjectListViewController.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		// open settings
		robot.clickOn(launchDialog.getOpenSettingsButton());
		WaitForAsyncUtils.waitForFxEvents();

		verify(programSettingsViewController).showAndWait(any(), any());
		assertThat(stage.isShowing()).isTrue();
	}
}
