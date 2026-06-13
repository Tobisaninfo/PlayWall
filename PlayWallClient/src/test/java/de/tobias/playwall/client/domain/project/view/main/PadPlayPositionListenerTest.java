package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.AudioPadContent;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.pad.update.PadLoadedUpdate;
import de.tobias.playwall.common.api.pad.update.PadPlayPositionUpdate;
import de.tobias.playwall.common.api.pad.update.PadStatusUpdate;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PadPlayPositionListenerTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private UpdateMessageEventHandler eventHandler;

	private Project project;

	private final Client client = mock(Client.class);
	private final WarningFlashAnimation warningFlashAnimation = mock(WarningFlashAnimation.class);

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);
		context.registerLazySingleton(WarningFlashAnimation.class, _ -> warningFlashAnimation);

		eventHandler = context.get(UpdateMessageEventHandler.class);

		project = loadProject("projects/project_1.json");
	}

	// Play Position

	@Test
	void testPlayPositionListenerOnPlay()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 5000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padView.getTimeLabel().getText()).isEqualTo("0:05");
		assertThat(padView.getPlayBar().getProgress()).isEqualTo(0.5);

		final ClientProjectController controller = context.get(ClientProjectController.class);
		final ClientPadController padController = controller.getPadController(padId);

		assertThat(padController.getPosition()).isEqualTo(Duration.millis(5000L));
	}

	@Test
	void testPlayPositionListenerOnPlayOffStage()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
			mainViewController.showPage(1);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		assertThat(padView).isNull();

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 5000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		final ClientProjectController controller = context.get(ClientProjectController.class);
		final ClientPadController padController = controller.getPadController(padId);

		assertThat(padController.getPosition()).isEqualTo(Duration.millis(5000L));
	}

	@Test
	void testPlayPositionListenerOnReady()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 5000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padView.getTimeLabel().getText()).isEqualTo("0:10");
		assertThat(padView.getPlayBar().getProgress()).isZero();
	}

	// Eof Warning

	@Test
	void testEofWarningWithProjectSettingsInvoked()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));
		WaitForAsyncUtils.waitForFxEvents();

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 6000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		verify(warningFlashAnimation).start();
	}

	@Test
	void testEofWarningWithProjectSettingsNotInvokedWhileLooping()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		((AudioPadContent) project.getPad(padId).getContent()).setLoop(true);


		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));
		WaitForAsyncUtils.waitForFxEvents();

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 6000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		verify(warningFlashAnimation, never()).start();
	}

	@Test
	void testEofWarningWithProjectSettingsInvokedOnlyOnce()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));
		WaitForAsyncUtils.waitForFxEvents();

		eventHandler.fireEvent(new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 6000L))));
		WaitForAsyncUtils.waitForFxEvents();
		when(warningFlashAnimation.isRunning()).thenReturn(true);
		eventHandler.fireEvent(new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 7000L))));
		WaitForAsyncUtils.waitForFxEvents();

		verify(warningFlashAnimation, times(1)).start();
	}

	@Test
	void testEofWarningWithProjectSettingsNotInvoked()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));
		WaitForAsyncUtils.waitForFxEvents();

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 4000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		verify(warningFlashAnimation, never()).start();
	}

	@Test
	void testEofWarningWithPadSettingsInvoked()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		project.getPad(padId).setEofWarningTime(3.0);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));
		WaitForAsyncUtils.waitForFxEvents();

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 8000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		verify(warningFlashAnimation).start();
	}

	@Test
	void testEofWarningWithPadSettingsNotInvoked()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		project.getPad(padId).setEofWarningTime(3.0);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));
		WaitForAsyncUtils.waitForFxEvents();

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 6000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		verify(warningFlashAnimation, never()).start();
	}
}
