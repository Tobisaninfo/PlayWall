package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.AudioPadContent;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.common.api.common.TimeMode;
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
import static org.mockito.Mockito.*;

class MainViewControllerPadSettingsTest extends AbstractViewControllerTest
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

	@Test
	void testOpenSettingsAndSetName(FxRobot robot) throws PlayWallApiException
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		context.registerLazy(Stage.class, _ -> new Stage());
		robot.clickOn(padView.getSettingsButton());

		WaitForAsyncUtils.waitForFxEvents();

		final List<Window> windows = new ArrayList<>(robot.listWindows());
		final Stage stageSettings = (Stage) windows.getLast();

		assertThat(stageSettings.getTitle()).isEqualTo("Kacheleinstellungen - 1 | Test Pad");

		robot.lookup("#textFieldName").queryAs(TextField.class).setText("Lorem");
		robot.clickOn("#saveButton");

		verify(client).updateSettings(padId, Pad.builder()
				.name("Lorem")
				.id(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"))
				.position(0)
				.timeMode(TimeMode.ELAPSED)
				.content(AudioPadContent.builder()
						.mediaPath("abc.mp3")
						.isLoop(false)
						.volume(1.0)
						.build())
				.build());
	}

	@Test
	void testOpenSettingsAndSetNameAndCancel(FxRobot robot) throws PlayWallApiException
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		context.registerLazy(Stage.class, _ -> new Stage());
		robot.clickOn(padView.getSettingsButton());

		WaitForAsyncUtils.waitForFxEvents();

		final List<Window> windows = new ArrayList<>(robot.listWindows());
		final Stage stageSettings = (Stage) windows.getLast();

		assertThat(stageSettings.getTitle()).isEqualTo("Kacheleinstellungen - 1 | Test Pad");

		robot.lookup("#textFieldName").queryAs(TextField.class).setText("Lorem");
		robot.clickOn("#cancelButton");

		verify(client, never()).updateSettings(padId, Pad.builder()
				.name("Lorem")
				.id(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"))
				.position(0)
				.timeMode(TimeMode.ELAPSED)
				.content(AudioPadContent.builder()
						.isLoop(false)
						.volume(1.0)
						.build())
				.build());
	}

	@Test
	void testOpenSettingsAndSetNameTryToSelectNewFile(FxRobot robot)
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		context.registerLazy(Stage.class, _ -> new Stage());
		robot.clickOn(padView.getSettingsButton());

		WaitForAsyncUtils.waitForFxEvents();

		List<Window> windows = new ArrayList<>(robot.listWindows());
		Stage settingsStage = (Stage) windows.getLast();

		assertThat(settingsStage.getTitle()).isEqualTo("Kacheleinstellungen - 1 | Test Pad");

		robot.lookup("#textFieldName").queryAs(TextField.class).setText("Lorem");
		robot.clickOn("#buttonChooseFile");

		WaitForAsyncUtils.waitForFxEvents();

		windows = new ArrayList<>(robot.listWindows());
		settingsStage = (Stage) windows.getLast();

		assertThat(settingsStage.getTitle()).isEqualTo("Es existieren ungespeicherte Änderungen");
	}
}
