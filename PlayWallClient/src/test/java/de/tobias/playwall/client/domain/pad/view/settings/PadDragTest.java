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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PadDragTest extends AbstractViewControllerTest
{
	private interface Verifier
	{
		void verify(Client client, UUID pad1, UUID pad2) throws PlayWallApiException;
	}

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

	private static Stream<Arguments> provideArguments()
	{
		return Stream.of(
				of("Duplizieren", (Verifier) (Client client, UUID pad1, UUID pad2) -> verify(client).duplicatePad(pad1, pad2)),
				of("Ersetzen", (Verifier) (Client client, UUID pad1, UUID pad2) -> verify(client).movePad(pad1, pad2))
		);
	}

	@ParameterizedTest
	@MethodSource("provideArguments")
	void testDragSamePage(String label, Verifier verifier, FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		Node pad1 = mainViewController.getPadViewForPosition(0).getRootNode();
		Node pad2 = mainViewController.getPadViewForPosition(1).getRootNode();

		robot.moveTo(pad1);
		robot.press(MouseButton.PRIMARY);
		robot.moveTo(pad2);
		robot.moveTo(label);
		robot.release(MouseButton.PRIMARY);
		WaitForAsyncUtils.waitForFxEvents();

		verifier.verify(client, PAD_0_0, PAD_0_1);
	}

	@ParameterizedTest
	@MethodSource("provideArguments")
	void testDragDifferentPage(String label, Verifier verifier, FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		Node pad1 = mainViewController.getPadViewForPosition(0).getRootNode();
		Node pad2 = mainViewController.getPadViewForPosition(1).getRootNode();

		robot.moveTo(pad1);
		robot.press(MouseButton.PRIMARY);
		robot.moveTo("Page 2");
		robot.moveTo(pad2);
		robot.moveTo(label);
		robot.release(MouseButton.PRIMARY);
		WaitForAsyncUtils.waitForFxEvents();

		verifier.verify(client, PAD_0_0, PAD_1_1);
	}
}
