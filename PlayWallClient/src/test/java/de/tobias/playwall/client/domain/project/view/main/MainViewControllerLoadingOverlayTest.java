package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.common.api.pad.update.PadLoadedUpdate;
import de.tobias.playwall.common.api.project.update.ProjectLoadedUpdate;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDeviceInstance;
import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.testfx.assertions.api.Assertions.assertThat;

class MainViewControllerLoadingOverlayTest extends AbstractViewControllerTest
{
	private AppContext context;
	private UpdateMessageEventHandler eventHandler;

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

		eventHandler = context.get(UpdateMessageEventHandler.class);
		project = loadProject("projects/project_1.json");
	}

	@Test
	void testLoadingOverlay()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		mainViewController.getLoadingOverlay().resetAndSetPadCount(4);

		mainViewController.getSettingsController().setSettings(Settings.builder()
						.autoLoadLatestProjectOnStart(false)
						.selectedAudioDevice("My Audio Device")
						.unsavedChangesMode(UnsavedChangesMode.DISCARD)
				.build());
		mainViewController.getSettingsController().setOutputDevices(List.of(new AudioDeviceInstance("My Audio Device", null, false, false)));

		assertThat(mainViewController.getLoadingOverlay()).isVisible();
		assertThat(mainViewController.getLoadingOverlay().getProgressBar().getProgress()).isZero();

		eventHandler.fireEvent(new PadLoadedUpdate(UUID.randomUUID(), true));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mainViewController.getLoadingOverlay().getProgressBar().getProgress()).isEqualTo(0.25);

		eventHandler.fireEvent(new PadLoadedUpdate(UUID.randomUUID(), true));
		eventHandler.fireEvent(new PadLoadedUpdate(UUID.randomUUID(), true));
		eventHandler.fireEvent(new PadLoadedUpdate(UUID.randomUUID(), true));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mainViewController.getLoadingOverlay().getProgressBar().getProgress()).isEqualTo(1.0);

		eventHandler.fireEvent(new ProjectLoadedUpdate());
		WaitForAsyncUtils.waitForFxEvents();

		await()
				.atMost(1, SECONDS)
				.untilAsserted(() -> assertThat(mainViewController.getLoadingOverlay().isVisible()).isFalse());
	}

}
