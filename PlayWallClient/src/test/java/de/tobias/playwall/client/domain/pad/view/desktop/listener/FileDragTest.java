package de.tobias.playwall.client.domain.pad.view.desktop.listener;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.view.main.MainViewController;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.pad.update.PadStatusUpdate;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FileDragTest extends AbstractViewControllerTest
{
	private AppContext context;
	private final Client client = mock(Client.class);

	private UpdateMessageEventHandler eventHandler;

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
		eventHandler = context.get(UpdateMessageEventHandler.class);

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

	@Test
	void testFileDrop(FxRobot robot) throws URISyntaxException, PlayWallApiException
	{
		showMainView();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");

		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();
		final List<File> files = List.of(new File(mediaPath));

		final Node targetNode = mainViewController.getPadViewForPadId(padId).getRootNode();

		final Dragboard dragboard = mock(Dragboard.class);
		when(dragboard.hasFiles()).thenReturn(true);
		when(dragboard.getFiles()).thenReturn(files);
		when(dragboard.getContentTypes()).thenReturn(Set.of(DataFormat.FILES));

		robot.interact(() -> {
			{
				final Point2D scenePoint = sceneCenterOf(targetNode);
				final Point2D screenPoint = screenCenterOf(targetNode);
				fireDragEvent(targetNode, DragEvent.DRAG_ENTERED, dragboard, scenePoint, screenPoint);
				fireDragEvent(targetNode, DragEvent.DRAG_OVER, dragboard, scenePoint, screenPoint);
			}
			{
				final Node dropOption = robot.lookup("Audio").query();
				final Point2D scenePoint = sceneCenterOf(dropOption);
				final Point2D screenPoint = screenCenterOf(dropOption);

				fireDragEvent(dropOption, DragEvent.DRAG_ENTERED, dragboard, scenePoint, screenPoint);
				fireDragEvent(dropOption, DragEvent.DRAG_OVER, dragboard, scenePoint, screenPoint);
				fireDragEvent(dropOption, DragEvent.DRAG_DROPPED, dragboard, scenePoint, screenPoint);
			}
		});
		WaitForAsyncUtils.waitForFxEvents();
		verify(client).newMedia(padId, Paths.get(mediaPath));
	}

	@Test
	void testFileDropOnPlayingPad(FxRobot robot) throws URISyntaxException
	{
		showMainView();

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		eventHandler.fireEvent(new PadStatusUpdate(padId, PadControllerStatus.PLAYING));

		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();
		final List<File> files = List.of(new File(mediaPath));

		final Node targetNode = mainViewController.getPadViewForPadId(padId).getRootNode();

		final Dragboard dragboard = mock(Dragboard.class);
		when(dragboard.hasFiles()).thenReturn(true);
		when(dragboard.getFiles()).thenReturn(files);
		when(dragboard.getContentTypes()).thenReturn(Set.of(DataFormat.FILES));

		robot.interact(() -> {
			{
				final Point2D scenePoint = sceneCenterOf(targetNode);
				final Point2D screenPoint = screenCenterOf(targetNode);
				fireDragEvent(targetNode, DragEvent.DRAG_ENTERED, dragboard, scenePoint, screenPoint);
				fireDragEvent(targetNode, DragEvent.DRAG_OVER, dragboard, scenePoint, screenPoint);
			}
		});
		WaitForAsyncUtils.waitForFxEvents();
		assertThat(robot.lookup("Audio").queryAll()).isEmpty();
	}


	private Point2D sceneCenterOf(Node node)
	{
		Bounds boundsInScene = node.localToScene(node.getBoundsInLocal());
		double x = boundsInScene.getMinX() + boundsInScene.getWidth() / 2;
		double y = boundsInScene.getMinY() + boundsInScene.getHeight() / 2;
		return new Point2D(x, y);
	}

	private Point2D screenCenterOf(Node node)
	{
		Bounds boundsOnScreen = node.localToScreen(node.getBoundsInLocal());
		double x = boundsOnScreen.getMinX() + boundsOnScreen.getWidth() / 2;
		double y = boundsOnScreen.getMinY() + boundsOnScreen.getHeight() / 2;
		return new Point2D(x, y);
	}

	private void fireDragEvent(Node target, EventType<DragEvent> type, Dragboard dragboard, Point2D scene, Point2D screen)
	{
		DragEvent event = new DragEvent(
				type, dragboard,
				scene.getX(), scene.getY(),
				screen.getX(), screen.getY(),
				TransferMode.COPY,
				null, target, null
		);
		Event.fireEvent(target, event);
	}
}
