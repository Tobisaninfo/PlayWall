package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.pad.update.PadStatusUpdate;
import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MainViewControllerCloseTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private UpdateMessageEventHandler eventHandler;

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
		eventHandler = context.get(UpdateMessageEventHandler.class);

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
	void testCloseHasPlayingPads(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		when(client.isSaved()).thenReturn(true);
		when(settingsController.getSettings()).thenReturn(Settings.builder()
				.autoLoadLatestProjectOnStart(false)
				.unsavedChangesMode(UnsavedChangesMode.ASK)
				.build());

		eventHandler.fireEvent(new PadStatusUpdate(UUID.fromString("fc427184-2e55-4734-8148-5fb657963616"), PadControllerStatus.PLAY));

		robot.interact(() -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));

		assertThat(stage.isShowing()).isTrue();
	}

	@Test
	void testCloseIsSaved(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		when(client.isSaved()).thenReturn(true);
		when(settingsController.getSettings()).thenReturn(Settings.builder()
				.autoLoadLatestProjectOnStart(false)
				.unsavedChangesMode(UnsavedChangesMode.ASK)
				.build());

		robot.interact(() -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));

		assertThat(stage.isShowing()).isFalse();
	}

	@Test
	void testCloseHasChangesDiscard(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		when(client.isSaved()).thenReturn(false);
		when(settingsController.getSettings()).thenReturn(Settings.builder()
				.autoLoadLatestProjectOnStart(false)
				.unsavedChangesMode(UnsavedChangesMode.DISCARD)
				.build());

		robot.interact(() -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));

		assertThat(stage.isShowing()).isFalse();
		verify(client, never()).saveProject();
	}

	@Test
	void testCloseHasChangesSave(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		when(client.isSaved()).thenReturn(false);
		when(settingsController.getSettings()).thenReturn(Settings.builder()
				.autoLoadLatestProjectOnStart(false)
				.unsavedChangesMode(UnsavedChangesMode.SAVE)
				.build());

		robot.interact(() -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));

		assertThat(stage.isShowing()).isFalse();
		verify(client).saveProject();
	}

	@Test
	void testCloseHasChangesAsk(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		when(client.isSaved()).thenReturn(false);
		when(settingsController.getSettings()).thenReturn(Settings.builder()
				.autoLoadLatestProjectOnStart(false)
				.unsavedChangesMode(UnsavedChangesMode.ASK)
				.build());

		robot.interact(() -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));

		assertThat(stage.isShowing()).isTrue();
	}
}
