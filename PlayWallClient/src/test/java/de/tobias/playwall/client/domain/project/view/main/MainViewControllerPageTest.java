package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testfx.assertions.api.Assertions.assertThat;

class MainViewControllerPageTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private Project project;

	private final Client client = mock(Client.class);

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

	@Test
	void testPageButtons(FxRobot robot)
	{
		showMainView();

		// Assert buttons
		assertThat(robot.lookup(".page-button").queryAll()).hasSize(2);
		assertThat(robot.lookup(".page-button").queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactlyInAnyOrder("Page 1", "Page 2");

		// Assert highlighting
		assertThat(mainViewController.getPageButtons().getChildren().getFirst().getStyleClass()).contains(ViewConstants.PAGE_BUTTON_CURRENT_STYLECLASS);
		assertThat(mainViewController.getPageButtons().getChildren().get(1).getStyleClass()).doesNotContain(ViewConstants.PAGE_BUTTON_CURRENT_STYLECLASS);

		// Assert current page
		final DesktopPadView padView1 = (DesktopPadView) mainViewController.getPadViewForPosition(0);
		assertThat(padView1.getNamePreviewLabel()).hasText("Test Pad");

		// Switch page
		robot.clickOn(mainViewController.getPageButtons().getChildren().get(1));
		WaitForAsyncUtils.waitForFxEvents();

		// Assert highlighting
		assertThat(mainViewController.getPageButtons().getChildren().getFirst().getStyleClass()).doesNotContain(ViewConstants.PAGE_BUTTON_CURRENT_STYLECLASS);
		assertThat(mainViewController.getPageButtons().getChildren().get(1).getStyleClass()).contains(ViewConstants.PAGE_BUTTON_CURRENT_STYLECLASS);

		// Assert current page
		final DesktopPadView padView2 = (DesktopPadView) mainViewController.getPadViewForPosition(0);
		assertThat(padView2.getNamePreviewLabel().getText()).isEmpty();
	}

	@Test
	void testPageDelete(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		final ContextMenu contextMenu = ((Button) mainViewController.getPageButtons().getChildren().getFirst()).getContextMenu();
		final MenuItem deleteMenuItem = contextMenu.getItems().get(2);
		robot.interact(deleteMenuItem::fire);

		final ArgumentCaptor<UUID> argumentCaptor = ArgumentCaptor.forClass(UUID.class);
		verify(client).deletePage(argumentCaptor.capture());

		assertThat(argumentCaptor.getValue()).isEqualTo(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"));
	}

	@Test
	void testPageDuplicate(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		final ContextMenu contextMenu = ((Button) mainViewController.getPageButtons().getChildren().getFirst()).getContextMenu();
		final MenuItem deleteMenuItem = contextMenu.getItems().get(1);
		robot.interact(deleteMenuItem::fire);

		final ArgumentCaptor<UUID> argumentCaptor = ArgumentCaptor.forClass(UUID.class);
		verify(client).duplicatePage(argumentCaptor.capture());

		assertThat(argumentCaptor.getValue()).isEqualTo(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"));
	}

	@Test
	void testPageReorder(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		// Assert buttons
		assertThat(robot.lookup(".page-button").queryAll()).hasSize(2);
		assertThat(robot.lookup(".page-button").queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 1", "Page 2");

		final Node button1 = robot.lookup(".page-button").nth(0).queryAs(Node.class);
		final Node button2 = robot.lookup(".page-button").nth(1).queryAs(Node.class);


		Point2D destination = button2.localToScreen(
				button2.getBoundsInLocal().getCenterX(),
				button2.getBoundsInLocal().getCenterY()
		);

		robot.drag(button1).moveTo(destination).drop();
		WaitForAsyncUtils.waitForFxEvents();

		//noinspection unchecked
		final ArgumentCaptor<Map<UUID, Integer>> argumentCaptor = ArgumentCaptor.forClass(Map.class);
		verify(client).reorderPage(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue()).containsExactlyInAnyOrderEntriesOf(Map.of(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 1,
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e"), 0
		));

		assertThat(robot.lookup(".page-button").queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 2", "Page 1");
	}

	@Test
	void testPageRename(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		final ContextMenu contextMenu = ((Button) mainViewController.getPageButtons().getChildren().getFirst()).getContextMenu();
		final MenuItem deleteMenuItem = contextMenu.getItems().getFirst();
		Platform.runLater(() -> robot.interact(deleteMenuItem::fire));
		WaitForAsyncUtils.waitForFxEvents();

		final TextInputControl textInputControl = robot.lookup(".text-input").queryTextInputControl();
		textInputControl.setText("New Page Name");
		robot.clickOn(robot.lookup("OK").lookup(".button").queryButton());
		WaitForAsyncUtils.waitForFxEvents();

		final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
		verify(client).renamePage(eq(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea")), argumentCaptor.capture());
		assertThat(argumentCaptor.getValue()).isEqualTo("New Page Name");
	}
}
