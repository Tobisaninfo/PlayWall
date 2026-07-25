package de.tobias.playwall.client.domain.pad.view.settings;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PadDragTest extends AbstractViewControllerTest
{
	public static final UUID PAD_0_0 = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
	public static final UUID PAD_0_1 = UUID.fromString("57accabc-7d19-473c-a0a1-ea5c61b85e18");
	public static final UUID PAD_1_1 = UUID.fromString("94ad54b2-995a-4da6-87c8-b86e5b97d03e");
	private AppContext context;
	private final Client client = mock(Client.class);

	private MainViewController mainViewController;
	private Stage stage;

	private Project project;

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);

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
	}

	// Duplicate

	@Test
	void testDragDuplicateSamePage(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		Node pad1 = mainViewController.getPadViewForPosition(0).getRootNode();
		Node pad2 = mainViewController.getPadViewForPosition(1).getRootNode();

		robot.moveTo(pad1);
		robot.press(MouseButton.PRIMARY);
		robot.moveTo(pad2);
		robot.moveTo("Duplizieren");
		robot.release(MouseButton.PRIMARY);
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).duplicatePad(PAD_0_0, PAD_0_1);
	}

	@Test
	void testDragDuplicateDifferentPage(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		Node pad1 = mainViewController.getPadViewForPosition(0).getRootNode();
		Node pad2 = mainViewController.getPadViewForPosition(1).getRootNode();

		robot.moveTo(pad1);
		robot.press(MouseButton.PRIMARY);
		robot.moveTo("Page 2");
		robot.moveTo(pad2);
		robot.moveTo("Duplizieren");
		robot.release(MouseButton.PRIMARY);
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).duplicatePad(PAD_0_0, PAD_1_1);
	}

	// Move

	@Test
	void testDragMoveSamePage(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		Node pad1 = mainViewController.getPadViewForPosition(0).getRootNode();
		Node pad2 = mainViewController.getPadViewForPosition(1).getRootNode();

		robot.moveTo(pad1);
		robot.press(MouseButton.PRIMARY);
		robot.moveTo(pad2);
		robot.moveTo("Ersetzen");
		robot.release(MouseButton.PRIMARY);
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).movePad(PAD_0_0, PAD_0_1);
	}

	@Test
	void testDragMoveDifferentPage(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		Node pad1 = mainViewController.getPadViewForPosition(0).getRootNode();
		Node pad2 = mainViewController.getPadViewForPosition(1).getRootNode();

		robot.moveTo(pad1);
		robot.press(MouseButton.PRIMARY);
		robot.moveTo("Page 2");
		robot.moveTo(pad2);
		robot.moveTo("Ersetzen");
		robot.release(MouseButton.PRIMARY);
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).movePad(PAD_0_0, PAD_1_1);
	}
}
