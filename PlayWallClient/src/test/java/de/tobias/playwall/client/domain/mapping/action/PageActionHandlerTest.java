package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.midi.feedback.DefaultFeedbackState;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.net.Client;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;

class PageActionHandlerTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	protected Project project;
	protected ClientProjectController projectController;

	protected final Client client = mock(Client.class);
	private PageActionHandler actionHandler;

	@Start
	public void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);

		project = loadProject("projects/project_1.json");
		projectController = context.get(ClientProjectController.class);
		actionHandler = context.get(PageActionHandler.class);
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

	// Previous

	@Test
	void testPreviousPageWhenBeingAtPageZero(FxRobot robot)
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new PageAction(PageAction.PageActionMode.PREVIOUS, null)
		);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(projectController.getCurrentPage().getPosition()).isZero();

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(projectController.getCurrentPage().getPosition()).isZero();
	}

	@Test
	void testPreviousPageWhenBeingAtPageOne(FxRobot robot)
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new PageAction(PageAction.PageActionMode.PREVIOUS, null)
		);
		projectController.setCurrentPageIndex(1);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(projectController.getCurrentPage().getPosition()).isEqualTo(1);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(projectController.getCurrentPage().getPosition()).isZero();
	}

	// Next

	@Test
	void testNextPageWhenBeingAtPageZero(FxRobot robot)
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new PageAction(PageAction.PageActionMode.NEXT, null)
		);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(projectController.getCurrentPage().getPosition()).isZero();

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(projectController.getCurrentPage().getPosition()).isEqualTo(1);
	}

	@Test
	void testNextPageWhenBeingAtPageOne(FxRobot robot)
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new PageAction(PageAction.PageActionMode.NEXT, null)
		);
		projectController.setCurrentPageIndex(1);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(projectController.getCurrentPage().getPosition()).isEqualTo(1);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(projectController.getCurrentPage().getPosition()).isEqualTo(1);
	}

	// Jump


	@Test
	void testJumoPage(FxRobot robot)
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new PageAction(PageAction.PageActionMode.JUMP, 0)
		);
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.B, "B"),
				new PageAction(PageAction.PageActionMode.JUMP, 1)
		);
		projectController.setCurrentPageIndex(1);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(projectController.getCurrentPage().getPosition()).isZero();

		robot.press(KeyCode.B);
		WaitForAsyncUtils.waitForFxEvents();
		robot.release(KeyCode.B);
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(projectController.getCurrentPage().getPosition()).isEqualTo(1);
	}

	// Feedback

	@Test
	void testCurrentFeedbackNext()
	{
		showMainView();

		final PageAction action = new PageAction(PageAction.PageActionMode.NEXT, null);
		project.getMetadata().getActiveMapping().addInputKeyWithAction(new KeyboardInputKey(KeyCode.A, "A"), action);

		assertThat(actionHandler.getCurrentState(action)).isEqualTo(DefaultFeedbackState.NORMAL);
	}

	@Test
	void testCurrentFeedbackPrevious()
	{
		showMainView();

		final PageAction action = new PageAction(PageAction.PageActionMode.PREVIOUS, null);
		project.getMetadata().getActiveMapping().addInputKeyWithAction(new KeyboardInputKey(KeyCode.A, "A"), action);

		assertThat(actionHandler.getCurrentState(action)).isEqualTo(DefaultFeedbackState.NORMAL);
	}

	@Test
	void testCurrentFeedbackJump()
	{
		showMainView();

		final PageAction action = new PageAction(PageAction.PageActionMode.JUMP, 1);
		project.getMetadata().getActiveMapping().addInputKeyWithAction(new KeyboardInputKey(KeyCode.A, "A"), action);

		assertThat(actionHandler.getCurrentState(action)).isEqualTo(DefaultFeedbackState.NORMAL);

		projectController.setCurrentPageIndex(1);
		assertThat(actionHandler.getCurrentState(action)).isEqualTo(DefaultFeedbackState.ACTIVE);
	}

}
