package de.tobias.playwall.client.domain.project.view.main;

import de.thecodelabs.midi.mapping.Mapping;
import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.FadeSettings;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();
	}

	@Test
	void testProjectSettingsChangeName(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		robot.clickOn(robot.lookup(".menu").lookup("Datei").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Projekteinstellungen...").queryLabeled());

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
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(4)
				.volume(1.0)
				.timeMode(TimeMode.ELAPSED)
				.defaultColor(ModernColor.GRAY1)
				.playColor(ModernColor.RED3)
				.introColor(ModernColor.BLUE1)
				.eofWarningTime(5.0)
				.fadeSettings(new FadeSettings())
				.mappings(Map.of(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"), new Mapping()))
				.selectedMapping(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"))
				.build());
	}

	@Test
	void testProjectSettingsChangeNameAndCancel(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		robot.clickOn(robot.lookup(".menu").lookup("Datei").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Projekteinstellungen...").queryLabeled());

		WaitForAsyncUtils.waitForFxEvents();

		final List<Window> windows = new ArrayList<>(robot.listWindows());
		final Stage stageSettings = (Stage) windows.getLast();

		assertThat(stageSettings.getTitle()).isEqualTo("Projekteinstellungen - Project 1");

		robot.lookup("#textFieldName").queryAs(TextField.class).setText("Fancy project name");
		robot.clickOn("#cancelButton");

		WaitForAsyncUtils.waitForFxEvents();

		verify(client, never()).updateProjectSettings(ProjectMetadata.builder()
				.name("Fancy project name")
				.id(UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c"))
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(4)
				.timeMode(TimeMode.ELAPSED)
				.defaultColor(ModernColor.GRAY1)
				.playColor(ModernColor.RED3)
				.introColor(null)
				.fadeSettings(new FadeSettings())
				.mappings(Map.of(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"), new Mapping()))
				.selectedMapping(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"))
				.build());
	}

	@Test
	void testProjectSettingsSameName(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		robot.clickOn(robot.lookup(".menu").lookup("Datei").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Projekteinstellungen...").queryLabeled());

		WaitForAsyncUtils.waitForFxEvents();

		final List<Window> windows = new ArrayList<>(robot.listWindows());
		final Stage stageSettings = (Stage) windows.getLast();

		assertThat(stageSettings.getTitle()).isEqualTo("Projekteinstellungen - Project 1");

		robot.lookup("#textFieldName").queryAs(TextField.class).setText("Project 1");
		robot.clickOn("#saveButton");

		WaitForAsyncUtils.waitForFxEvents();

		verify(client).updateProjectSettings(ProjectMetadata.builder()
				.name("Project 1")
				.id(UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c"))
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(4)
				.volume(1.0)
				.timeMode(TimeMode.ELAPSED)
				.defaultColor(ModernColor.GRAY1)
				.playColor(ModernColor.RED3)
				.introColor(ModernColor.BLUE1)
				.eofWarningTime(5.0)
				.fadeSettings(new FadeSettings())
				.mappings(Map.of(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"), new Mapping()))
				.selectedMapping(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"))
				.build());
	}

	@Test
	void testProjectSettingsChangeNameAlreadyExists(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		robot.clickOn(robot.lookup(".menu").lookup("Datei").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Projekteinstellungen...").queryLabeled());

		WaitForAsyncUtils.waitForFxEvents();

		final List<Window> windows = new ArrayList<>(robot.listWindows());
		final Stage stageSettings = (Stage) windows.getLast();

		doThrow(new PlayWallApiException("Das Projekt mit dem Namen \"Project 2\" konnte nicht angelegt werden. Es existiert bereits ein Projekt mit diesem Namen.", new ProjectNameAlreadyExistsError("Project 2")))
				.when(client).updateProjectSettings(any());

		assertThat(stageSettings.getTitle()).isEqualTo("Projekteinstellungen - Project 1");

		robot.lookup("#textFieldName").queryAs(TextField.class).setText("Project 2");
		robot.clickOn("#saveButton");

		WaitForAsyncUtils.waitForFxEvents();

		Assertions.assertThat(robot.lookup(".label.content").queryLabeled()).hasText("Das Projekt mit dem Namen \"Project 2\" konnte nicht angelegt werden. Es existiert bereits ein Projekt mit diesem Namen.");
	}

	@Test
	void testProjectSettingsChangeTimeMode(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		robot.clickOn(robot.lookup(".menu").lookup("Datei").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Projekteinstellungen...").queryLabeled());

		WaitForAsyncUtils.waitForFxEvents();

		final List<Window> windows = new ArrayList<>(robot.listWindows());
		final Stage stageSettings = (Stage) windows.getLast();

		assertThat(stageSettings.getTitle()).isEqualTo("Projekteinstellungen - Project 1");

		final Button settingsCategoryView = robot.lookup(".settings-category").nth(1).queryAs(Button.class);
		robot.clickOn(settingsCategoryView);

		WaitForAsyncUtils.waitForFxEvents();

		final ComboBox<?> comboBoxTime = robot.lookup("#comboBoxTime").queryAs(ComboBox.class);
		assertThat(comboBoxTime.getSelectionModel().getSelectedItem()).isEqualTo(TimeMode.ELAPSED);

		robot.interact(() -> comboBoxTime.getSelectionModel().select(1));
		robot.clickOn("#saveButton");

		WaitForAsyncUtils.waitForFxEvents();

		verify(client).updateProjectSettings(ProjectMetadata.builder()
				.name("Project 1")
				.id(UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c"))
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(4)
				.volume(1.0)
				.timeMode(TimeMode.REMAINING)
				.defaultColor(ModernColor.GRAY1)
				.playColor(ModernColor.RED3)
				.introColor(ModernColor.BLUE1)
				.eofWarningTime(5.0)
				.fadeSettings(new FadeSettings())
				.mappings(Map.of(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"), new Mapping()))
				.selectedMapping(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"))
				.build());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testProjectSettingsChangeNumberOfPads(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		robot.clickOn(robot.lookup(".menu").lookup("Datei").queryLabeled());
		robot.clickOn(robot.lookup(".menu-item").lookup("Projekteinstellungen...").queryLabeled());

		WaitForAsyncUtils.waitForFxEvents();

		final List<Window> windows = new ArrayList<>(robot.listWindows());
		final Stage stageSettings = (Stage) windows.getLast();

		assertThat(stageSettings.getTitle()).isEqualTo("Projekteinstellungen - Project 1");

		robot.lookup("#spinnerNumberOfHorizontalPads").queryAs(Spinner.class).getValueFactory().setValue(7);
		robot.lookup("#spinnerNumberOfVerticalPads").queryAs(Spinner.class).getValueFactory().setValue(5);
		robot.clickOn("#saveButton");

		WaitForAsyncUtils.waitForFxEvents();

		verify(client).updateProjectSettings(ProjectMetadata.builder()
				.name("Project 1")
				.id(UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c"))
				.numberOfHorizontalPads(7)
				.numberOfVerticalPads(5)
				.volume(1.0)
				.timeMode(TimeMode.ELAPSED)
				.defaultColor(ModernColor.GRAY1)
				.playColor(ModernColor.RED3)
				.introColor(ModernColor.BLUE1)
				.eofWarningTime(5.0)
				.fadeSettings(new FadeSettings())
				.mappings(Map.of(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"), new Mapping()))
				.selectedMapping(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"))
				.build());
	}
}
