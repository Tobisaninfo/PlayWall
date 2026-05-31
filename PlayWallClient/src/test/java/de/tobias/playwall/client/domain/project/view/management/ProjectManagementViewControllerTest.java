package de.tobias.playwall.client.domain.project.view.management;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.*;
import de.tobias.playwall.client.domain.project.view.ProjectNewDialog;
import de.tobias.playwall.client.domain.project.view.list.ProjectListViewController;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.utils.ExportFile;
import de.tobias.playwall.client.utils.MimeType;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.testfx.assertions.api.Assertions.assertThat;

class ProjectManagementViewControllerTest extends AbstractViewControllerTest
{
	private static final UUID PROJECT_ID_1 = UUID.randomUUID();
	private static final ProjectMetadata PROJECT_METADATA_1 = new ProjectMetadata(PROJECT_ID_1, "Test 1", 6, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3, ModernColor.LIGHT_GREEN2, null, new FadeSettings());

	private static final UUID PROJECT_ID_2 = UUID.randomUUID();
	private static final ProjectMetadata PROJECT_METADATA_2 = new ProjectMetadata(PROJECT_ID_2, "Test 2", 6, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3, ModernColor.LIGHT_GREEN2, null, new FadeSettings());

	@TempDir
	private Path tempDir;

	private AppContext context;
	private final Client client = mock(Client.class);

	private ProjectManagementViewController viewController;

	private final MainViewController mainViewController = mock(MainViewController.class);
	private final ClientProjectController projectController = mock(ClientProjectController.class);
	private final ProjectNewDialog projectNewDialog = mock(ProjectNewDialog.class);
	private final ProjectListViewController projectListViewController = mock(ProjectListViewController.class);
	private final FileChooserWrapper fileChooserWrapper = mock(FileChooserWrapper.class);

	private Stage stage;

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);
		context.registerLazySingleton(MainViewController.class, _ -> mainViewController);
		context.registerLazySingleton(ProjectNewDialog.class, _ -> projectNewDialog);
		context.registerLazySingleton(ProjectListViewController.class, _ -> projectListViewController);
		context.registerLazySingleton(ClientProjectController.class, _ -> projectController);
		context.registerLazySingleton(FileChooserWrapper.class, _ -> fileChooserWrapper);
	}

	private void openStage()
	{
		Platform.runLater(() -> {
			viewController = context.get(ProjectManagementViewController.class);
			viewController.initParameter(new ProjectManagementViewController.Param(mainViewController));
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();
	}

	@Test
	void testListProjects() throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1, PROJECT_METADATA_2))
				.recentProjectIds(List.of(PROJECT_ID_1))
				.build());
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		openStage();

		assertThat(viewController.getProjectListView()).hasExactlyNumItems(2);
		assertThat(viewController.getProjectListView()).hasExactlyNumItems(2);
	}

	@Test
	void testOpenProject(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of(PROJECT_ID_1))
				.build());
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		openStage();

		final Node cell = viewController.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		robot.doubleClickOn(cell, Motion.DEFAULT);
		WaitForAsyncUtils.waitForFxEvents();

		verify(mainViewController).closeCurrentProjectAndOpenProject(PROJECT_ID_1);
		assertThat(stage.isShowing()).isFalse();
	}

	@Test
	void testNewProject(FxRobot robot) throws PlayWallApiException
	{
		ProjectMetadata metadata = new ProjectMetadata(PROJECT_METADATA_2.getId(), "Test 1", 4, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3, ModernColor.LIGHT_GREEN2, null, new FadeSettings());
		when(projectNewDialog.showAndWait(any())).thenReturn(Optional.of(metadata));

		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of(PROJECT_ID_1))
				.build());
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		openStage();

		robot.clickOn(viewController.getNewButton());
		WaitForAsyncUtils.waitForFxEvents();

		verify(mainViewController).closeCurrentProjectAndOpenProject(PROJECT_ID_2);
		assertThat(stage.isShowing()).isFalse();
	}

	@Test
	void testRenameProject(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		openStage();
		clearInvocations(client);

		final ProjectManagementCell cell = (ProjectManagementCell) viewController.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		Platform.runLater(() -> cell.getButtonContextMenu().getItems().getFirst().fire());
		WaitForAsyncUtils.waitForFxEvents();

		final TextInputControl textInputControl = robot.lookup(".text-input-dialog .text-field").queryTextInputControl();
		textInputControl.setText("New Name");
		robot.clickOn(robot.lookup("OK").lookup(".button").queryButton());
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).renameProject(PROJECT_ID_1, "New Name");
		verify(client).getProjects();
	}

	@Test
	void testRenameProjectInvalidInput(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1, PROJECT_METADATA_2))
				.recentProjectIds(List.of())
				.build());
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		openStage();
		clearInvocations(client);

		final ProjectManagementCell cell = (ProjectManagementCell) viewController.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		Platform.runLater(() -> cell.getButtonContextMenu().getItems().getFirst().fire());
		WaitForAsyncUtils.waitForFxEvents();

		final TextInputControl textInputControl = robot.lookup(".text-input-dialog").lookup(".text-input").queryTextInputControl();
		textInputControl.setText("Test 2");
		robot.clickOn(robot.lookup("OK").lookup(".button").queryButton());
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(robot.lookup(".text-input").queryTextInputControl()).isVisible();
		assertThat(robot.lookup(".error-label").queryLabeled()).hasText("Der Name des Projekts existiert bereits.");

		verify(client, never()).renameProject(any(), any());
	}

	@Test
	void testDuplicateProject() throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		openStage();
		clearInvocations(client);

		final ProjectManagementCell cell = (ProjectManagementCell) viewController.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		Platform.runLater(() -> cell.getButtonContextMenu().getItems().get(1).fire());
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).duplicateProject(PROJECT_ID_1);
		verify(client).getProjects();
	}

	@Test
	void testExportProject() throws PlayWallApiException, IOException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		openStage();

		final Path targetPath = tempDir.resolve("test.json");
		when(fileChooserWrapper.showSaveFile(any())).thenReturn(Optional.of(targetPath));
		when(client.exportProject(any())).thenReturn(new ExportFile(MimeType.APPLICATION_JSON.getMimeTypeValue(), new byte[]{1, 2, 3}));

		final ProjectManagementCell cell = (ProjectManagementCell) viewController.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		Platform.runLater(() -> cell.getButtonContextMenu().getItems().get(2).fire());
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(Files.exists(targetPath)).isTrue();
		assertThat(Files.readAllBytes(targetPath)).isEqualTo(new byte[]{1, 2, 3});
	}

	@Test
	void testImportProject() throws PlayWallApiException, IOException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		openStage();
		clearInvocations(client);

		final Path targetPath = tempDir.resolve("test.json");
		Files.write(targetPath, new byte[]{1, 2, 3});
		when(fileChooserWrapper.showOpenFile(any())).thenReturn(Optional.of(targetPath));

		viewController.onImportButton();

		final ArgumentCaptor<ExportFile> captor = ArgumentCaptor.forClass(ExportFile.class);
		verify(client).importProject(captor.capture());
		assertThat(captor.getValue())
				.satisfies(value -> {
					assertThat(value.mimetype()).isEqualTo("application/json");
					assertThat(value.data()).isEqualTo(new byte[]{1, 2, 3});
				});

		verify(client).getProjects();
	}

	@Test
	void testDeleteCurrentProject(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		openStage();

		final ProjectManagementCell cell = (ProjectManagementCell) viewController.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		Platform.runLater(() -> cell.getButtonContextMenu().getItems().get(4).fire());
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn(robot.lookup("OK").lookup(".button").queryButton());

		verify(mainViewController).closeStage();
		assertThat(stage.isShowing()).isFalse();
		verify(projectListViewController).showStage();

		verify(client).deleteProject(PROJECT_ID_1);
	}

	@Test
	void testDeletePlayingProject(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_1, List.of()));
		when(projectController.isAtLeastOnePadPlaying()).thenReturn(true);

		openStage();

		final ProjectManagementCell cell = (ProjectManagementCell) viewController.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		Platform.runLater(() -> cell.getButtonContextMenu().getItems().get(4).fire());
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(robot.lookup(".label.content").queryLabeled()).hasText("Das Projekt kann nicht geschlossen werden, solange noch Kacheln wiedergegeben werden!");
		robot.clickOn(robot.lookup("OK").lookup(".button").queryButton());

		verify(client, never()).deleteProject(PROJECT_ID_1);
	}

	@Test
	void testDeleteOtherProject(FxRobot robot) throws PlayWallApiException
	{
		when(client.getProjects()).thenReturn(AllProjectsInfo.builder()
				.allProjectsMetadata(List.of(PROJECT_METADATA_1))
				.recentProjectIds(List.of())
				.build());
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_2, List.of()));

		openStage();

		final ProjectManagementCell cell = (ProjectManagementCell) viewController.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		Platform.runLater(() -> cell.getButtonContextMenu().getItems().get(4).fire());
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn(robot.lookup("OK").lookup(".button").queryButton());

		verify(mainViewController, never()).closeStage();
		assertThat(stage.isShowing()).isTrue();
		verify(projectListViewController, never()).showStage();

		verify(client).deleteProject(PROJECT_ID_1);
	}
}
