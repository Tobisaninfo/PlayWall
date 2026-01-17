package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.model.project.PadStatus;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.service.ClientPadController;
import de.tobias.playwall.client.service.ClientProjectController;
import de.tobias.playwall.client.viewcontroller.AbstractViewControllerTest;
import de.tobias.playwall.client.viewcontroller.main.desktop.DesktopPadView;
import de.tobias.playwall.common.api.project.PadStatusUpdate;
import de.tobias.playwall.common.api.project.model.PadControllerStatus;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static org.testfx.assertions.api.Assertions.assertThat;

class PadStatusListenerTest extends AbstractViewControllerTest
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
	void testPadUpdateListener()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);

		final ClientPadController padController = context.get(ClientProjectController.class).getPadController(padId);
		padController.setDuration(Duration.millis(10000L));

		// Initial state = READY
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPlayButton(), padView.getStopButton(), padView.getSettingsButton());

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAY));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPauseButton(), padView.getStopButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel()).hasText("0:10");
		assertThat(padController.getStatus()).isEqualTo(PadStatus.PLAY);

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PAUSE));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPlayButton(), padView.getStopButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel()).hasText("0:10");
		assertThat(padController.getStatus()).isEqualTo(PadStatus.PAUSE);

		padController.setPosition(Duration.millis(5000L));

		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.STOP));
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(padView.getButtonBox().getChildren()).contains(padView.getPlayButton(), padView.getStopButton(), padView.getSettingsButton());
		assertThat(padView.getTimeLabel()).hasText("0:10");
		assertThat(padController.getStatus()).isEqualTo(PadStatus.READY);
		assertThat(padController.getPosition()).isEqualTo(Duration.ZERO); // Check that the play position is set to zero

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
}
