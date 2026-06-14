package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.common.api.common.TimeMode;
import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.pad.update.PadLoadedUpdate;
import de.tobias.playwall.common.api.pad.update.PadPlayPositionUpdate;
import de.tobias.playwall.common.api.pad.update.PadStatusUpdate;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TimeModeTest extends AbstractViewControllerTest
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

		context.registerLazy(Client.class, _ -> client);

		eventHandler = context.get(UpdateMessageEventHandler.class);

		project = loadProject("projects/project_1.json");
	}

	@Test
	void testTimeModeElapsed()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		padView.getPadController().getPad().setTimeMode(TimeMode.ELAPSED);

		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 5000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padView.getTimeLabel().getText()).isEqualTo("0:05");
	}

	@Test
	void testTimeModeRemaining()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		padView.getPadController().getPad().setTimeMode(TimeMode.REMAINING);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 3000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padView.getTimeLabel().getText()).isEqualTo("-0:07");
	}

	@Test
	void testTimeModeElapsedAndTotal()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		padView.getPadController().getPad().setTimeMode(TimeMode.ELAPSED_AND_TOTAL);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 3000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padView.getTimeLabel().getText()).isEqualTo("0:03 / 0:10");
	}

	@Test
	void testTimeModeUseProjectSettings()
	{
		project.getMetadata().setTimeMode(TimeMode.REMAINING);

		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		padView.getPadController().getPad().setTimeMode(null);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));
		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 3000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padView.getTimeLabel().getText()).isEqualTo("-0:07");
	}
}
