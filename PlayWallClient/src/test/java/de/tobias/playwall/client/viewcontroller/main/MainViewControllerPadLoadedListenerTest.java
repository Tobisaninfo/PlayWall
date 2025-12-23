package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.viewcontroller.AbstractViewControllerTest;
import de.tobias.playwall.client.viewcontroller.main.desktop.DesktopPadView;
import de.tobias.playwall.common.api.project.PadLoadedUpdate;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.testfx.assertions.api.Assertions.assertThat;

class MainViewControllerPadLoadedListenerTest extends AbstractViewControllerTest
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
	}

	@Test
	void testPadUpdateListener()
	{
		final AboutDialog dialog = mock(AboutDialog.class);
		AppContextHolder.getInstance().registerLazySingleton(AboutDialog.class, _ -> dialog);

		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.openProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);

		eventHandler.fireEvent(new PadLoadedUpdate(padId, false));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padView.getBusyView().getIndicator()).isVisible();
		assertThat(padView.getBusyView().getIndicator().getParent().getParent()).isNotNull();

		eventHandler.fireEvent(new PadLoadedUpdate(padId, false));
		WaitForAsyncUtils.waitForFxEvents();

		// Check if busy view is removed from node tree
		await()
				.atMost(2, SECONDS)
				.untilAsserted(() -> assertThat(padView.getBusyView().getIndicator().getParent().getParent()).isNull());
	}
}
