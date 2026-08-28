package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.FileChooserWrapper;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;

import static org.mockito.Mockito.*;

class GlobalVolumeActionHandlerTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private Project project;

	private final Client client = mock(Client.class);
	private final FileChooserWrapper fileChooserWrapper = mock(FileChooserWrapper.class);

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);
		context.registerLazySingleton(FileChooserWrapper.class, _ -> fileChooserWrapper);

		project = loadProject("projects/project_1.json");
	}

	private void showMainView()
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

	static List<GlobalVolumeAction.VolumeChangeDelta> params()
	{
		return List.of(GlobalVolumeAction.VolumeChangeDelta.FIVE, GlobalVolumeAction.VolumeChangeDelta.TEN);
	}

	@ParameterizedTest
	@MethodSource("params")
	void testGlobalVolumeActionHandlerDecrease(GlobalVolumeAction.VolumeChangeDelta delta, FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new GlobalVolumeAction(GlobalVolumeAction.VolumeChangeMode.DECREASE, delta)
		);

		double before = project.getMetadata().getVolume();

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).changeGlobalVolume(before);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).changeGlobalVolume(before - delta.getDelta());
	}

	@Test
	void testGlobalVolumeActionHandlerDecreaseLowerBound(FxRobot robot) throws PlayWallApiException
	{
		project.getMetadata().setVolume(0.02);
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new GlobalVolumeAction(GlobalVolumeAction.VolumeChangeMode.DECREASE, GlobalVolumeAction.VolumeChangeDelta.FIVE)
		);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).changeGlobalVolume(0);

		clearInvocations(client);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).changeGlobalVolume(0);
	}

	@ParameterizedTest
	@MethodSource("params")
	void testGlobalVolumeActionHandlerIncrease(GlobalVolumeAction.VolumeChangeDelta delta, FxRobot robot) throws PlayWallApiException
	{
		project.getMetadata().setVolume(0.5);
		showMainView();

		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new GlobalVolumeAction(GlobalVolumeAction.VolumeChangeMode.INCREASE, delta)
		);

		double before = project.getMetadata().getVolume();

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).changeGlobalVolume(before);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).changeGlobalVolume(before + delta.getDelta());
	}


	@Test
	void testGlobalVolumeActionHandlerDecreaseUpperBound(FxRobot robot) throws PlayWallApiException
	{
		project.getMetadata().setVolume(0.98);
		showMainView();
		project.getMetadata().getActiveMapping().addInputKeyWithAction(
				new KeyboardInputKey(KeyCode.A, "A"),
				new GlobalVolumeAction(GlobalVolumeAction.VolumeChangeMode.INCREASE, GlobalVolumeAction.VolumeChangeDelta.FIVE)
		);

		robot.press(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).changeGlobalVolume(1);

		clearInvocations(client);

		robot.release(KeyCode.A);
		WaitForAsyncUtils.waitForFxEvents();
		verify(client, never()).changeGlobalVolume(1);
	}
}
