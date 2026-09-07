package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.midi.feedback.DefaultFeedbackState;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StopAllActionHandlerTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	protected Project project;

	protected final Client client = mock(Client.class);
	private StopAllActionHandler actionHandler;

	@Start
	public void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);

		project = loadProject("projects/project_1.json");
		actionHandler = context.get(StopAllActionHandler.class);
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

	@Test
	void testStopAllAction(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new StopAllAction()
		);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).stopAllPads();

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).stopAllPads();
	}

	// Feedback

	@Test
	void testCurrentFeedback()
	{
		showMainView();

		final StopAllAction action = new StopAllAction();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(new KeyboardInputKey(KeyCode.A, "A"), action);

		assertThat(actionHandler.getCurrentState(action)).isEqualTo(DefaultFeedbackState.NORMAL);
	}
}
