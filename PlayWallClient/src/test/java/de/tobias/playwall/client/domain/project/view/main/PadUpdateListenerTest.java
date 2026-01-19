package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.view.about.AboutDialog;
import de.tobias.playwall.common.api.pad.PadDto;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.testfx.assertions.api.Assertions.assertThat;

class PadUpdateListenerTest extends AbstractViewControllerTest
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
		ClientProjectController projectController = context.get(ClientProjectController.class);
		projectController.loadProject(project);
	}

	@Test
	void testPadUpdateListener()
	{
		final AboutDialog dialog = mock(AboutDialog.class);
		AppContextHolder.getInstance().registerLazySingleton(AboutDialog.class, _ -> dialog);

		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);

		assertThat(padView.getNamePreviewLabel()).hasText("Test Pad");

		final PadDto newPad = PadDto.builder().id(padId).position(0).name("Updated Pad").build();
		eventHandler.fireEvent(new PadUpdate(newPad));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(padView.getNamePreviewLabel()).hasText("Updated Pad");
	}
}
