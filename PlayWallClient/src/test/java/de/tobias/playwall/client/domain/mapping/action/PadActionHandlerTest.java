package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.midi.feedback.DefaultFeedbackState;
import de.tobias.playwall.client.domain.pad.PadStatus;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PadActionHandlerTest extends AbstractViewControllerTest
{
	public static final UUID PAD_0_0 = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
	public static final UUID PAD_1_0 = UUID.fromString("33ee14cf-56d8-42bf-9aee-f5e3f22d2193");
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	protected Project project;
	protected ClientProjectController projectController;

	protected final Client client = mock(Client.class);
	private PadActionHandler actionHandler;

	@Start
	public void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);

		project = loadProject("projects/project_1.json");
		projectController = context.get(ClientProjectController.class);
		actionHandler = context.get(PadActionHandler.class);
	}

	protected void showMainView()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();
		clearInvocations(client);
	}

	// Play Stop

	@Test
	void testPlayStopWithFixPage(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new PadAction(PadAction.PadActionMode.PLAY_STOP, UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 0)
		);

		// Play
		projectController.getPadController(PAD_0_0).setStatus(PadStatus.READY);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).play(PAD_0_0);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).play(PAD_0_0);

		// Stop
		projectController.getPadController(PAD_0_0).setStatus(PadStatus.PLAYING);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).stop(PAD_0_0);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).stop(PAD_0_0);
	}

	@Test
	void testPlayStopWithFlexiblePage(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new PadAction(PadAction.PadActionMode.PLAY_STOP, null, 0)
		);
		projectController.setCurrentPageIndex(1);

		// Play 0n Page 1
		projectController.getPadController(PAD_0_0).setStatus(PadStatus.READY);
		projectController.getPadController(PAD_1_0).setStatus(PadStatus.READY);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).play(PAD_1_0);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).play(PAD_1_0);

		// Stop on page 0
		projectController.setCurrentPageIndex(0);
		projectController.getPadController(PAD_0_0).setStatus(PadStatus.PLAYING);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).stop(PAD_0_0);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).stop(PAD_0_0);
	}

	// Play Pause

	@Test
	void testPlayPauseWithFixPage(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new PadAction(PadAction.PadActionMode.PLAY_PAUSE, UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 0)
		);

		// Play
		projectController.getPadController(PAD_0_0).setStatus(PadStatus.READY);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).play(PAD_0_0);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).play(PAD_0_0);

		// Pause
		projectController.getPadController(PAD_0_0).setStatus(PadStatus.PLAYING);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).pause(PAD_0_0);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).pause(PAD_0_0);
	}

	// Play Play

	@Test
	void testPlayPlayWithFixPage(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new PadAction(PadAction.PadActionMode.PLAY_PLAY, UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 0)
		);

		// Play
		projectController.getPadController(PAD_0_0).setStatus(PadStatus.READY);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).play(PAD_0_0);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).play(PAD_0_0);

		clearInvocations(client);

		// Play
		projectController.getPadController(PAD_0_0).setStatus(PadStatus.PLAYING);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).play(PAD_0_0);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).stopImmediately(PAD_0_0);
		verify(client).play(PAD_0_0);
	}

	// Play Hold

	@Test
	void testPlayHoldWithFixPage(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new PadAction(PadAction.PadActionMode.PLAY_HOLD, UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 0)
		);

		// Play
		projectController.getPadController(PAD_0_0).setStatus(PadStatus.READY);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).play(PAD_0_0);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).stop(PAD_0_0);
	}

	// Feedback

	@Test
	void testCurrentFeedbackFixedPage()
	{
		showMainView();

		final PadAction action = new PadAction(PadAction.PadActionMode.PLAY_STOP, UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 0);
		project.getMetadata().getActiveMapping().addInputKeyWithAction(new KeyboardInputKey(KeyCode.A, "A"), action);

		assertThat(actionHandler.getCurrentState(action)).isNull();

		projectController.getPadController(PAD_0_0).setStatus(PadStatus.READY);
		assertThat(actionHandler.getCurrentState(action)).isEqualTo(DefaultFeedbackState.NORMAL);

		projectController.getPadController(PAD_0_0).setStatus(PadStatus.PLAYING);
		assertThat(actionHandler.getCurrentState(action)).isEqualTo(DefaultFeedbackState.ACTIVE);

		projectController.getPadController(PAD_0_0).setStatus(PadStatus.STOPPING);
		assertThat(actionHandler.getCurrentState(action)).isEqualTo(DefaultFeedbackState.WARNING);
	}

	@Test
	void testCurrentFeedbackFlexiblePage()
	{
		showMainView();

		final PadAction action = new PadAction(PadAction.PadActionMode.PLAY_STOP, null, 0);
		project.getMetadata().getActiveMapping().addInputKeyWithAction(new KeyboardInputKey(KeyCode.A, "A"), action);

		projectController.getPadController(PAD_0_0).setStatus(PadStatus.READY);
		projectController.getPadController(PAD_1_0).setStatus(PadStatus.PLAYING);

		assertThat(actionHandler.getCurrentState(action)).isEqualTo(DefaultFeedbackState.NORMAL);

		projectController.setCurrentPageIndex(1);
		assertThat(actionHandler.getCurrentState(action)).isEqualTo(DefaultFeedbackState.ACTIVE);
	}
}
