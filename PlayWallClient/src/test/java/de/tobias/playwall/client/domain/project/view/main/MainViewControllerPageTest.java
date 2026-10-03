package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.page.PageSettings;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.utils.ExportFile;
import de.tobias.playwall.client.utils.MimeType;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.components.PseudoClasses;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static de.tobias.playwall.client.view.components.ViewConstants.PAGE_BUTTON_STYLECLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testfx.assertions.api.Assertions.assertThat;

class MainViewControllerPageTest extends AbstractViewControllerTest
{
	private static final String PAGE_BUTTON_QUERY = "." + PAGE_BUTTON_STYLECLASS;

	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private Project project;

	private final Client client = mock(Client.class);
	private final FileChooserWrapper fileChooserWrapper = mock(FileChooserWrapper.class);

	@TempDir
	private Path tempDir;

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
	}

	@Test
	void testPageButtons(FxRobot robot)
	{
		showMainView();

		// Assert buttons
		assertThat(robot.lookup(PAGE_BUTTON_QUERY).queryAll()).hasSize(2);
		assertThat(robot.lookup(PAGE_BUTTON_QUERY).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactlyInAnyOrder("Page 1", "Page 2");

		// Assert highlighting
		assertThat(mainViewController.getPageButtons().getChildren().getFirst().getPseudoClassStates()).contains(PseudoClasses.SELECTED);
		assertThat(mainViewController.getPageButtons().getChildren().get(1).getPseudoClassStates()).doesNotContain(PseudoClasses.SELECTED);

		// Assert page button color
		assertThat(mainViewController.getPageButtons().getChildren().getFirst().getStyleClass()).contains(PAGE_BUTTON_STYLECLASS, "GRAY1");
		assertThat(mainViewController.getPageButtons().getChildren().get(1).getStyleClass()).contains(PAGE_BUTTON_STYLECLASS, "GRAY2");

		// Assert current page
		final DesktopPadView padView1 = (DesktopPadView) mainViewController.getPadViewForPosition(0);
		assertThat(padView1.getNamePreviewLabel()).hasText("Test Pad");

		// Switch page
		robot.clickOn(mainViewController.getPageButtons().getChildren().get(1));
		WaitForAsyncUtils.waitForFxEvents();

		// Assert highlighting
		assertThat(mainViewController.getPageButtons().getChildren().getFirst().getPseudoClassStates()).doesNotContain(PseudoClasses.SELECTED);
		assertThat(mainViewController.getPageButtons().getChildren().get(1).getPseudoClassStates()).contains(PseudoClasses.SELECTED);

		// Assert current page
		final DesktopPadView padView2 = (DesktopPadView) mainViewController.getPadViewForPosition(0);
		assertThat(padView2.getNamePreviewLabel().getText()).isEmpty();
	}

	@Test
	void testPageAdd(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		robot.clickOn(mainViewController.getPageAddButton());
		final MenuItem menuItem = mainViewController.getPageAddButtonContextMenu().getItems().getFirst();
		robot.interact(menuItem::fire);
		WaitForAsyncUtils.waitForFxEvents();

		verify(client).addPage();
	}

	@Test
	void testPageDelete(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		final ContextMenu contextMenu = ((Button) mainViewController.getPageButtons().getChildren().getFirst()).getContextMenu();
		final MenuItem menuItem = contextMenu.getItems().get(4);
		robot.interact(menuItem::fire);

		final ArgumentCaptor<UUID> argumentCaptor = ArgumentCaptor.forClass(UUID.class);
		verify(client).deletePage(argumentCaptor.capture());

		assertThat(argumentCaptor.getValue()).isEqualTo(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"));
	}

	@Test
	void testPageDuplicate(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		final ContextMenu contextMenu = ((Button) mainViewController.getPageButtons().getChildren().getFirst()).getContextMenu();
		final MenuItem menuItem = contextMenu.getItems().get(1);
		robot.interact(menuItem::fire);

		final ArgumentCaptor<UUID> argumentCaptor = ArgumentCaptor.forClass(UUID.class);
		verify(client).duplicatePage(argumentCaptor.capture());

		assertThat(argumentCaptor.getValue()).isEqualTo(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"));
	}

	@Test
	void testPageReorder(FxRobot robot) throws PlayWallApiException
	{
		showMainView();

		// Assert buttons
		assertThat(robot.lookup(PAGE_BUTTON_QUERY).queryAll()).hasSize(2);
		assertThat(robot.lookup(PAGE_BUTTON_QUERY).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 1", "Page 2");

		final Node button1 = robot.lookup(PAGE_BUTTON_QUERY).nth(0).queryAs(Node.class);
		final Node button2 = robot.lookup(PAGE_BUTTON_QUERY).nth(1).queryAs(Node.class);

		robot.drag(button1).moveTo(button2, new Point2D(20, 0)).drop();
		WaitForAsyncUtils.waitForFxEvents();

		//noinspection unchecked
		final ArgumentCaptor<Map<UUID, Integer>> argumentCaptor = ArgumentCaptor.forClass(Map.class);
		verify(client).reorderPage(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue()).containsExactlyInAnyOrderEntriesOf(Map.of(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 1,
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e"), 0
		));

		assertThat(robot.lookup(PAGE_BUTTON_QUERY).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 2", "Page 1");
	}

	@Test
	void testPageRename(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		final ContextMenu contextMenu = ((Button) mainViewController.getPageButtons().getChildren().getFirst()).getContextMenu();
		final MenuItem menuItem = contextMenu.getItems().getFirst();
		Platform.runLater(() -> robot.interact(menuItem::fire));
		WaitForAsyncUtils.waitForFxEvents();

		final TextInputControl textInputControl = robot.lookup(".text-input").queryTextInputControl();
		Platform.runLater(() -> textInputControl.setText("New Page Name"));
		WaitForAsyncUtils.waitForFxEvents();
		robot.clickOn(robot.lookup("Speichern").queryButton());
		WaitForAsyncUtils.waitForFxEvents();

		final ArgumentCaptor<PageSettings> argumentCaptor = ArgumentCaptor.forClass(PageSettings.class);
		verify(client).updatePageSettings(eq(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea")), argumentCaptor.capture());
		assertThat(argumentCaptor.getValue().getName()).isEqualTo("New Page Name");
	}

	@Test
	void testPageRenameEmptyTextField(FxRobot robot) throws PlayWallApiException
	{
		showMainView();
		context.registerLazy(Stage.class, _ -> new Stage());

		final ContextMenu contextMenu = ((Button) mainViewController.getPageButtons().getChildren().getFirst()).getContextMenu();
		final MenuItem menuItem = contextMenu.getItems().getFirst();
		Platform.runLater(() -> robot.interact(menuItem::fire));
		WaitForAsyncUtils.waitForFxEvents();

		final TextInputControl textInputControl = robot.lookup(".text-input").queryTextInputControl();
		Platform.runLater(() -> textInputControl.setText(""));
		WaitForAsyncUtils.waitForFxEvents();
		robot.clickOn(robot.lookup("Speichern").queryButton());
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(robot.lookup(".error-label").queryLabeled()).hasText("Der Name der Seite darf nicht leer sein.");

		verify(client, never()).updatePageSettings(any(), any());
	}

	@Test
	void testPageExport(FxRobot robot) throws PlayWallApiException, IOException
	{
		showMainView();

		final Path targetPath = tempDir.resolve("test.json");
		when(fileChooserWrapper.showSaveFile(any())).thenReturn(Optional.of(targetPath));
		when(client.exportPage(any())).thenReturn(new ExportFile(MimeType.APPLICATION_JSON.getMimeTypeValue(), new byte[]{1, 2, 3}));

		final ContextMenu contextMenu = ((Button) mainViewController.getPageButtons().getChildren().getFirst()).getContextMenu();
		final MenuItem menuItem = contextMenu.getItems().get(2);
		Platform.runLater(() -> robot.interact(menuItem::fire));
		WaitForAsyncUtils.waitForFxEvents();

		Assertions.assertThat(Files.exists(targetPath)).isTrue();
		Assertions.assertThat(Files.readAllBytes(targetPath)).isEqualTo(new byte[]{1, 2, 3});
	}

	@Test
	void testPageImport(FxRobot robot) throws PlayWallApiException, IOException
	{
		showMainView();

		final Path targetPath = tempDir.resolve("test.json");
		Files.write(targetPath, new byte[]{1, 2, 3});
		when(fileChooserWrapper.showOpenFile(any())).thenReturn(Optional.of(targetPath));

		robot.clickOn(mainViewController.getPageAddButton());
		final MenuItem menuItem = mainViewController.getPageAddButtonContextMenu().getItems().get(1);
		robot.interact(menuItem::fire);
		WaitForAsyncUtils.waitForFxEvents();

		final ArgumentCaptor<ExportFile> captor = ArgumentCaptor.forClass(ExportFile.class);
		verify(client).importPage(captor.capture());
		assertThat(captor.getValue())
				.satisfies(value -> {
					assertThat(value.mimetype()).isEqualTo("application/json");
					assertThat(value.data()).isEqualTo(new byte[]{1, 2, 3});
				});
	}
}
