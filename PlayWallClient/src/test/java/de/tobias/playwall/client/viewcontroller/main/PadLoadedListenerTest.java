package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.service.ClientPadController;
import de.tobias.playwall.client.service.ClientProjectController;
import de.tobias.playwall.client.viewcontroller.AbstractViewControllerTest;
import de.tobias.playwall.client.viewcontroller.main.desktop.DesktopPadView;
import de.tobias.playwall.common.api.project.PadLoadedUpdate;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.testfx.assertions.api.Assertions.assertThat;

class PadLoadedListenerTest extends AbstractViewControllerTest
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

		eventHandler = context.get(UpdateMessageEventHandler.class);

		project = loadProject("projects/project_1.json");
		ClientProjectController projectController = context.get(ClientProjectController.class);
		projectController.loadProject(project);
	}

	@Test
	void testPadUpdateListenerLoadingAnimation()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);

		eventHandler.fireEvent(new PadLoadedUpdate(padId, false));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padView.getBusyView().getIndicator()).isVisible();

		eventHandler.fireEvent(new PadLoadedUpdate(padId, true));
		WaitForAsyncUtils.waitForFxEvents();

		// Check if a busy view is removed from a node tree
		await()
				.atMost(2, SECONDS)
				.untilAsserted(() -> assertThat(padView.getBusyView().getIndicator().getParent().getParent()).isNull());
	}

	@Test
	void testPadUpdateListenerPlayDuration()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);

		assertThat(padView.getTimeLabel().getText()).isNull();

		eventHandler.fireEvent(new PadLoadedUpdate(padId, true, 10000L));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padView.getTimeLabel().getText()).isEqualTo("0:10");

		final ClientProjectController controller = context.get(ClientProjectController.class);
		final ClientPadController padController = controller.getPadController(padId);

		Assertions.assertThat(padController.getDuration()).isEqualTo(Duration.millis(10000L));
	}
}
