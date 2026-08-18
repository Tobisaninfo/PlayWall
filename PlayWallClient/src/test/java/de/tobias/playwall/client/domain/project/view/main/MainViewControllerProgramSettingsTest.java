package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MainViewControllerProgramSettingsTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private Project project;

	private final Client client = mock(Client.class);
	private final ClientSettingsController settingsController = mock(ClientSettingsController.class);

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);
		context.registerLazySingleton(ClientSettingsController.class, _ -> settingsController);

		project = loadProject("projects/project_1.json");

		when(settingsController.getSettings()).thenReturn(Settings.builder()
				.autoLoadLatestProjectOnStart(false)
				.build());
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
	void testProgramSettings(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		robot.clickOn(robot.lookup(".menu").lookup("Datei").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Einstellungen...").queryLabeled());

		WaitForAsyncUtils.waitForFxEvents();

		final List<Window> windows = new ArrayList<>(robot.listWindows());
		final Stage stageSettings = (Stage) windows.getLast();

		assertThat(stageSettings.getTitle()).isEqualTo("Einstellungen");

		robot.interact(() -> robot.lookup("#checkboxStartAutoLoadLatestProject").queryAs(CheckBox.class).setSelected(true));
		final ComboBox<?> comboBoxUnsavedChanges = robot.lookup("#comboBoxUnsavedChanges").queryAs(ComboBox.class);
		robot.interact(() -> comboBoxUnsavedChanges.getSelectionModel().select(2));
		robot.clickOn("#saveButton");

		WaitForAsyncUtils.waitForFxEvents();

		verify(client).updateProgramSettings(Settings.builder()
				.autoLoadLatestProjectOnStart(true)
				.unsavedChangesMode(UnsavedChangesMode.DISCARD)
				.build());
	}
}
