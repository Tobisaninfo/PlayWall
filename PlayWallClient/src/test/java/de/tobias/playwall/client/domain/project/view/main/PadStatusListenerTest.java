package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.PadStatus;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.pad.update.PadStatusUpdate;
import de.tobias.playwall.common.api.project.update.ProjectLoadedUpdate;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.testfx.assertions.api.Assertions.assertThat;

class PadStatusListenerTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private UpdateMessageEventHandler eventHandler;

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
	@SuppressWarnings("java:S5961")
	void testPadUpdateListener()
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

		final ClientPadController padController = context.get(ClientProjectController.class).getPadController(padId);
		padController.setDuration(Duration.millis(10000L));

		// Initial state = READY
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPlayButton(), padView.getStopButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel()).hasText("0:10");

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.ERROR));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getSettingsButton());
		assertThat(padView.getTimeLabel().getText()).isNull();
		assertThat(padView.getErrorLabel().isVisible()).isTrue();
		assertThat(padController.getStatus()).isEqualTo(PadStatus.ERROR);

		padController.setPosition(Duration.millis(5000L));

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPauseButton(), padView.getStopButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel()).hasText("0:05");
		assertThat(padController.getStatus()).isEqualTo(PadStatus.PLAYING);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PAUSING));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPlayButton(), padView.getStopButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel()).hasText("0:05");
		assertThat(padController.getStatus()).isEqualTo(PadStatus.PAUSING);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PAUSED));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPlayButton(), padView.getStopButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel()).hasText("0:05");
		assertThat(padController.getStatus()).isEqualTo(PadStatus.PAUSED);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.STOPPING));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPlayButton(), padView.getStopButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel()).hasText("0:05");
		assertThat(padController.getStatus()).isEqualTo(PadStatus.STOPPING);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.STOPPED));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPlayButton(), padView.getStopButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel()).hasText("0:10");
		assertThat(padController.getStatus()).isEqualTo(PadStatus.STOPPED);

		padController.setPosition(Duration.millis(5000L));

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.EOF));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPlayButton(), padView.getStopButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel()).hasText("0:10");
		assertThat(padController.getStatus()).isEqualTo(PadStatus.READY);
		assertThat(padController.getPosition()).isEqualTo(Duration.ZERO); // Check that the play position is set to zero

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPlayButton(), padView.getStopButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel()).hasText("0:10");
		assertThat(padController.getStatus()).isEqualTo(PadStatus.READY);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.EMPTY));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getNewButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel().getText()).isNull();
		assertThat(padController.getStatus()).isEqualTo(PadStatus.EMPTY);
		assertThat(padController.getPosition()).isNull();
		assertThat(padController.getDuration()).isNull();
	}

	@Test
	void testPadErrorsToastClosesWhenNoErrorsRemain()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final ClientSettingsController settingsController = context.get(ClientSettingsController.class);
		settingsController.setSettings(Settings.builder().build());
		settingsController.setOutputDevices(List.of());

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.ERROR));
		WaitForAsyncUtils.waitForFxEvents();

		eventHandler.fireEvent(new ProjectLoadedUpdate(null));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(mainViewController.getPadErrorsToast()).isNotNull();

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(mainViewController.getPadErrorsToast()).isNull();
	}
}
