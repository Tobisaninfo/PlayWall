package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.AudioPadContent;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
	}

	@Test
	void testOpenSettingsAndSetName(FxRobot robot) throws PlayWallApiException
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
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

		robot.interact(() -> robot.lookup("#textFieldName").queryAs(TextField.class).setText("Lorem"));
		robot.clickOn("#saveButton");

		verify(client).updateSettings(padId, Pad.builder()
				.name("Lorem")
				.id(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"))
				.position(0)
				.timeMode(null)
				.defaultColor(null)
				.playColor(null)
				.introColor(null)
				.eofWarningTime(null)
				.introDuration(0.0)
				.content(AudioPadContent.builder()
						.mediaPath("abc.mp3")
						.isLoop(false)
						.speed(1.0)
						.volume(1.0)
						.isIgnoreSoloMode(false)
						.build())
				.build());
	}

	@Test
	void testOpenSettingsAndSetNameAndCancel(FxRobot robot) throws PlayWallApiException
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
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

		robot.interact(() -> robot.lookup("#textFieldName").queryAs(TextField.class).setText("Lorem"));
		robot.clickOn("#cancelButton");

		verify(client, never()).updateSettings(padId, Pad.builder()
				.name("Lorem")
				.id(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"))
				.position(0)
				.timeMode(TimeMode.ELAPSED)
				.defaultColor(ModernColor.GRAY1)
				.playColor(ModernColor.RED3)
				.introColor(null)
				.content(AudioPadContent.builder()
						.isLoop(false)
						.speed(1.0)
						.volume(1.0)
						.isIgnoreSoloMode(false)
						.build())
				.build());
	}

	@Test
	void testOpenSettingsAndSetNameTryToSelectNewFile(FxRobot robot)
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
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

		robot.interact(() -> robot.lookup("#textFieldName").queryAs(TextField.class).setText("Lorem"));
		robot.clickOn("#buttonChooseFile");

		WaitForAsyncUtils.waitForFxEvents();

		windows = new ArrayList<>(robot.listWindows());
		settingsStage = (Stage) windows.getLast();

		assertThat(settingsStage.getTitle()).isEqualTo("Es existieren ungespeicherte Änderungen");
	}

	@Test
	void testProjectSettingsChangeTimeMode(FxRobot robot) throws PlayWallApiException
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		context.registerLazy(Stage.class, _ -> new Stage());
		robot.clickOn(padView.getSettingsButton());

		WaitForAsyncUtils.waitForFxEvents();

		List<Window> windows = new ArrayList<>(robot.listWindows());
		Stage currentStage = (Stage) windows.getLast();

		assertThat(currentStage.getTitle()).isEqualTo("Kacheleinstellungen - 1 | Test Pad");

		final Button settingsCategoryView = robot.lookup(".settings-category").nth(1).queryAs(Button.class);
		robot.clickOn(settingsCategoryView);

		WaitForAsyncUtils.waitForFxEvents();

		final ComboBox<?> comboBoxTime = robot.lookup("#comboBoxTime").queryAs(ComboBox.class);
		assertThat(comboBoxTime.getSelectionModel().getSelectedItem()).isNull();

		robot.interact(() -> comboBoxTime.getSelectionModel().select(1));
		robot.clickOn("#saveButton");

		WaitForAsyncUtils.waitForFxEvents();

		verify(client, never()).updateSettings(padId, Pad.builder()
				.name("Test Pad")
				.id(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"))
				.position(0)
				.timeMode(TimeMode.REMAINING)
				.defaultColor(ModernColor.GRAY1)
				.playColor(ModernColor.RED3)
				.introColor(null)
				.content(AudioPadContent.builder()
						.isLoop(false)
						.speed(1.0)
						.volume(1.0)
						.isIgnoreSoloMode(false)
						.build())
				.build());
	}

	@Test
	void testProjectSettingsChangeTimeModeToNull(FxRobot robot) throws PlayWallApiException
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		context.registerLazy(Stage.class, _ -> new Stage());
		robot.clickOn(padView.getSettingsButton());

		WaitForAsyncUtils.waitForFxEvents();

		List<Window> windows = new ArrayList<>(robot.listWindows());
		Stage currentStage = (Stage) windows.getLast();

		assertThat(currentStage.getTitle()).isEqualTo("Kacheleinstellungen - 1 | Test Pad");

		final Button settingsCategoryView = robot.lookup(".settings-category").nth(1).queryAs(Button.class);
		robot.clickOn(settingsCategoryView);

		WaitForAsyncUtils.waitForFxEvents();

		final ComboBox<?> comboBoxTime = robot.lookup("#comboBoxTime").queryAs(ComboBox.class);
		assertThat(comboBoxTime.getSelectionModel().getSelectedItem()).isNull();

		robot.interact(() -> comboBoxTime.getSelectionModel().select(1));
		robot.clickOn("#saveButton");

		WaitForAsyncUtils.waitForFxEvents();

		verify(client, never()).updateSettings(padId, Pad.builder()
				.name("Test Pad")
				.id(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"))
				.position(0)
				.timeMode(null)
				.defaultColor(ModernColor.GRAY1)
				.playColor(ModernColor.RED3)
				.introColor(null)
				.content(AudioPadContent.builder()
						.isLoop(false)
						.speed(1.0)
						.volume(1.0)
						.isIgnoreSoloMode(false)
						.build())
				.build());
	}
}
