package de.tobias.playwall.client.domain.project.view.management;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.AllProjectsInfo;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.project.view.ProjectNewDialog;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
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

class ProjectManagementViewControllerTest extends AbstractViewControllerTest
{
	private static final UUID PROJECT_ID_1 = UUID.randomUUID();
	private static final ProjectMetadata PROJECT_METADATA_1 = new ProjectMetadata(PROJECT_ID_1, "Test 1", 6, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3, ModernColor.LIGHT_GREEN2, null);

	private static final UUID PROJECT_ID_2 = UUID.randomUUID();
	private static final ProjectMetadata PROJECT_METADATA_2 = new ProjectMetadata(PROJECT_ID_2, "Test 2", 6, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3, ModernColor.LIGHT_GREEN2, null);

	private AppContext context;
	private final Client client = mock(Client.class);

	private ProjectManagementViewController viewController;

	private final MainViewController mainViewController = mock(MainViewController.class);
	private final ClientProjectController projectController = mock(ClientProjectController.class);
	private final ProjectNewDialog projectNewDialog = mock(ProjectNewDialog.class);

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
		context.registerLazySingleton(ClientProjectController.class, _ -> projectController);
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
	void testListProjects(FxRobot robot) throws PlayWallApiException
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
		ProjectMetadata metadata = new ProjectMetadata(PROJECT_METADATA_2.getId(), "Test 1", 4, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3, ModernColor.LIGHT_GREEN2, null);
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
	void testProjectDeleteOkay(FxRobot robot) throws PlayWallApiException
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
		verify(client).deleteProject(PROJECT_ID_1);
	}
}
