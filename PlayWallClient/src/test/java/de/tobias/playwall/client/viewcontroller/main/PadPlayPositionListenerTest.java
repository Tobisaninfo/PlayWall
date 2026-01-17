package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.viewcontroller.AbstractViewControllerTest;
import de.tobias.playwall.client.viewcontroller.main.desktop.DesktopPadView;
import de.tobias.playwall.common.api.pad.update.PadLoadedUpdate;
import de.tobias.playwall.common.api.pad.update.PadPlayPositionUpdate;
import de.tobias.playwall.common.api.pad.update.PadStatusUpdate;
import de.tobias.playwall.common.api.pad.PadControllerStatus;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PadPlayPositionListenerTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private UpdateMessageEventHandler eventHandler;

	private Project project;

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		final FluentClient client = Mockito.mock(FluentClient.class);
		Mockito.when(client.currentProject()).thenReturn(Mockito.mock(FluentClient.ProjectCurrentBuilder.class));
		context.registerLazy(FluentClient.class, _ -> client);

		eventHandler = context.get(UpdateMessageEventHandler.class);

		project = loadProject("projects/project_1.json");
		ClientProjectController projectController = context.get(ClientProjectController.class);
		projectController.loadProject(project);
	}

	@Test
	void testPlayPositionListenerOnPlay()
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

		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAY));

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
			stage.show();
			mainViewController.showPage(1);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		assertThat(padView).isNull();

		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAY));

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
			stage.show();
			mainViewController.showPage(0);
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);

		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.READY));

		final PadPlayPositionUpdate update = new PadPlayPositionUpdate(List.of(new PadPlayPositionUpdate.PadPlayPosition(padId, 5000L)));
		eventHandler.fireEvent(update);
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padView.getTimeLabel().getText()).isEqualTo("0:10");
		assertThat(padView.getPlayBar().getProgress()).isZero();
	}
}
