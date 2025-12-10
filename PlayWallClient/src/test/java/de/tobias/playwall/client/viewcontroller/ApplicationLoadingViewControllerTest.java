package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationInfo;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.launch.ServerLauncher;
import de.tobias.playwall.client.net.Client;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.testfx.assertions.api.Assertions.assertThat;

class ApplicationLoadingViewControllerTest extends AbstractViewControllerTest
{
	private abstract static class StubClient implements Client
	{
		@Override
		@SneakyThrows
		@SuppressWarnings("java:S2925")
		public void connectWithRetries(int numberOfRetries, ConnectingListener listener)
		{
			listener.onFailure(1, 10);
			Thread.sleep(1000L);
			listener.onFailure(2, 10);
		}
	}

	private AppContext context;
	private final LaunchDialog launchDialog = mock(LaunchDialog.class);
	private final Client client = mock(StubClient.class);
	private final ServerLauncher serverLauncher = mock(ServerLauncher.class);

	private ApplicationLoadingViewController controller;
	private Stage stage;

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		final App app = mock(App.class);
		final ApplicationInfo appInfo = mock(ApplicationInfo.class);
		when(appInfo.getName()).thenReturn("PlayWall");
		when(appInfo.getVersion()).thenReturn("0.0.1");
		when(app.getInfo()).thenReturn(appInfo);
		context.registerLazySingleton(App.class, _ -> app);

		context.registerLazySingleton(ServerLauncher.class, _ -> serverLauncher);
		context.registerLazySingleton(Client.class, _ -> client);
		context.registerLazy(LaunchDialog.class, _ -> launchDialog);
	}

	@Test
	void testStartApplicationSuccessfully()
	{
		Mockito.doCallRealMethod().when(client).connectWithRetries(anyInt(), any());

		Platform.runLater(() -> {
			controller = context.get(ApplicationLoadingViewController.class);
			stage.show();
		});

		WaitForAsyncUtils.waitForFxEvents();
		assertThat(controller.getTitleLabel()).hasText("PlayWall");
		assertThat(controller.getVersionLabel()).hasText("0.0.1");

		// Verify server launch
		verify(serverLauncher).launchServer();

		// Verify client connection
		await()
				.atMost(2, SECONDS)
				.untilAsserted(() -> assertThat(controller.getLoadingLabel()).hasText("Verbindungsaufbau... (1 / 10)"));

		await()
				.atMost(2, SECONDS)
				.untilAsserted(() -> assertThat(controller.getLoadingLabel()).hasText("Verbindungsaufbau... (2 / 10)"));

		// Verify launch dialog gets opened
		verify(launchDialog).showStage();
	}

	@Test
	void testConnectToServerFailed(FxRobot robot)
	{
		doThrow(new RuntimeException("Cannot connect to server")).when(client).connectWithRetries(anyInt(), any());

		Platform.runLater(() -> {
			controller = context.get(ApplicationLoadingViewController.class);
			stage.show();
		});

		WaitForAsyncUtils.waitForFxEvents();
		assertThat(controller.getTitleLabel()).hasText("PlayWall");
		assertThat(controller.getVersionLabel()).hasText("0.0.1");

		// Verify server launch
		verify(serverLauncher).launchServer();

		assertThat(robot.lookup(".label.content").queryLabeled()).hasText("PlayWall konnte nicht gestartet werden. \n(Fehler: Cannot connect to server)");
		robot.clickOn(robot.lookup("OK").lookup(".button").queryButton());

		// Verify launch dialog gets opened
		verify(launchDialog, never()).showStage();
	}
}
