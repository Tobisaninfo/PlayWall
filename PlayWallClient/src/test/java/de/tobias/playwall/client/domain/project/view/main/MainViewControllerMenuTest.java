package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.AllProjectsInfo;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.project.view.ProjectNewDialog;
import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.about.AboutDialog;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.testfx.assertions.api.Assertions.assertThat;

class MainViewControllerMenuTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private Project project;
	private Project projectEmpty;
	private final ClientSettingsController settingsController = mock(ClientSettingsController.class);
	private final ProjectNewDialog projectNewDialog = mock(ProjectNewDialog.class);
	private final Client client = mock(Client.class);

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);
		context.registerLazySingleton(ClientSettingsController.class, _ -> settingsController);
		context.registerLazySingleton(ProjectNewDialog.class, _ -> projectNewDialog);
		when(settingsController.getSettings()).thenReturn(Settings.builder()
				.unsavedChangesMode(UnsavedChangesMode.DISCARD)
				.autoLoadLatestProjectOnStart(false)
				.build());

		project = loadProject("projects/project_1.json");
		projectEmpty = loadProject("projects/project_empty.json");
	}

	private void showMainView()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
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
	void testMenuAbout(FxRobot robot)
	{
		final AboutDialog dialog = mock(AboutDialog.class);
		AppContextHolder.getInstance().registerLazySingleton(AboutDialog.class, _ -> dialog);

		showMainView();

		robot.clickOn(robot.lookup(".menu").lookup("Info").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Über PlayWall").queryLabeled());

		verify(dialog).showAndWait(stage);
	}

	@Test
	void testMenuRecentFiles(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		final AllProjectsInfo allProjectsInfo = AllProjectsInfo.builder()
				.recentProjectIds(List.of(projectEmpty.getMetadata().getId(), project.getMetadata().getId()))
				.allProjectsMetadata(List.of(project.getMetadata(), projectEmpty.getMetadata()))
				.build();

		Platform.runLater(() -> mainViewController.updateMenuRecentProjects(allProjectsInfo));

		when(client.getProject(projectEmpty.getMetadata().getId())).thenReturn(projectEmpty);
		when(client.getProjects()).thenReturn(allProjectsInfo);

		robot.clickOn(robot.lookup(".menu").lookup("Datei").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Zuletzt verwendete Projekte").queryLabeled());
		assertThat(mainViewController.getMenuRecentProjects().getItems())
				.hasSize(1)
				.first()
				.extracting(MenuItem::getText)
				.isEqualTo("Project Empty");


		robot.clickOn(robot.lookup(".menu-item").lookup("Project Empty").queryLabeled());

		WaitForAsyncUtils.waitForFxEvents();

		assertThat(stage.getTitle()).isEqualTo("PlayWall - Project Empty");
	}

	@Test
	void testMenuNewProject(FxRobot robot) throws PlayWallApiException
	{
		ProjectMetadata metadata = new ProjectMetadata(projectEmpty.getMetadata().getId(), projectEmpty.getMetadata().getName(), 2, 2, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3, null);
		when(projectNewDialog.showAndWait(any())).thenReturn(Optional.of(metadata));
		when(client.getProject(projectEmpty.getMetadata().getId())).thenReturn(projectEmpty);

		final AllProjectsInfo allProjectsInfo = AllProjectsInfo.builder()
				.recentProjectIds(List.of(projectEmpty.getMetadata().getId(), project.getMetadata().getId()))
				.allProjectsMetadata(List.of(project.getMetadata(), projectEmpty.getMetadata()))
				.build();
		when(client.getProjects()).thenReturn(allProjectsInfo);

		showMainView();

		robot.clickOn(robot.lookup(".menu").lookup("Datei").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Neues Projekt").queryLabeled());
		WaitForAsyncUtils.waitForFxEvents();

		// open project
		assertThat(stage.getTitle()).isEqualTo("PlayWall - Project Empty");
	}
}
