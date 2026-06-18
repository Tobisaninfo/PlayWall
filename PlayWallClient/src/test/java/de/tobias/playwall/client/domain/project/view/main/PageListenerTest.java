package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.view.components.ViewConstants;
import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.pad.PadDto;
import de.tobias.playwall.common.api.page.PageDto;
import de.tobias.playwall.common.api.page.update.*;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PageListenerTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private UpdateMessageEventHandler eventHandler;

	private Project project;

	private final Client client = mock(Client.class);

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
	void testPageAddListener(FxRobot robot)
	{
		showMainView();

		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll()).hasSize(2);

		eventHandler.fireEvent(new PageAddUpdate(new PageDto(UUID.randomUUID(), "Page 3", Color.GRAY1, 1,
				IntStream.range(0, project.getMetadata().getNumberOfPadsPerPage())
						.mapToObj(i -> new PadDto(UUID.randomUUID(), i, null, null, null, Color.GRAY1, Color.RED3, Color.LIGHT_GREEN2, null, null, null)).toList())));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll()).hasSize(3);
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactlyInAnyOrder("Page 1", "Page 2", "Page 3");
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll()).last().satisfies(button -> assertThat(((Button) button).getStyleClass()).contains(ViewConstants.PAGE_BUTTON_STYLECLASS));
	}

	@Test
	void testPageDeleteListener(FxRobot robot)
	{
		showMainView();

		// Check precondition
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 1", "Page 2");
		assertThat(project.getPages()).extracting(Page::getId).containsExactly(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"),
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e")
		);

		// Perform action
		eventHandler.fireEvent(new PageDeleteUpdate(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), Map.of(
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e"), 0
		)));
		WaitForAsyncUtils.waitForFxEvents();

		// Verify
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 2");
		assertThat(project.getPages()).extracting(Page::getId).containsExactly(
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e")
		);
	}

	@Test
	void testPageInsertListener(FxRobot robot)
	{
		showMainView();

		// Check precondition
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 1", "Page 2");
		assertThat(project.getPages()).extracting(Page::getId).containsExactly(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"),
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e")
		);

		// Perform action
		final UUID newPageId = UUID.randomUUID();
		eventHandler.fireEvent(new PageInsertUpdate(new PageDto(newPageId, "Page 3", Color.GRAY1, 1,
				IntStream.range(0, project.getMetadata().getNumberOfPadsPerPage())
						.mapToObj(i -> new PadDto(UUID.randomUUID(), i, null, null, null, Color.GRAY1, Color.RED3, Color.LIGHT_GREEN2, null, null, null)).toList()),
				1, Map.of(
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e"), 2
		)));
		WaitForAsyncUtils.waitForFxEvents();

		// Verify
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 1", "Page 3", "Page 2");
		assertThat(project.getPages()).extracting(Page::getId).containsExactly(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"),
				newPageId,
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e")
		);
	}

	@Test
	void testPageReplaceListener(FxRobot robot)
	{
		showMainView();

		// Check precondition
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 1", "Page 2");
		assertThat(project.getPages()).extracting(Page::getId).containsExactly(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"),
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e")
		);

		// Perform action
		final UUID newPageId = UUID.randomUUID();
		eventHandler.fireEvent(new PageReplaceUpdate(new PageDto(newPageId, "Page 3", Color.GRAY1, 1,
				IntStream.range(0, project.getMetadata().getNumberOfPadsPerPage())
						.mapToObj(i -> new PadDto(UUID.randomUUID(), i, null, null, null, Color.GRAY1, Color.RED3, Color.LIGHT_GREEN2, null, null, null)).toList()),
				1));
		WaitForAsyncUtils.waitForFxEvents();

		// Verify
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 1", "Page 3");
		assertThat(project.getPages()).extracting(Page::getId).containsExactly(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"),
				newPageId
		);
	}

	@Test
	void testPageReorderListener(FxRobot robot)
	{
		showMainView();

		// Check precondition
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 1", "Page 2");
		assertThat(project.getPages()).extracting(Page::getId).containsExactly(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"),
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e")
		);

		// Perform action
		eventHandler.fireEvent(new PageReorderUpdate(Map.of(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 1,
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e"), 0
		)));
		WaitForAsyncUtils.waitForFxEvents();

		// Verify
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 2", "Page 1");
		assertThat(project.getPages()).extracting(Page::getId).containsExactly(
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e"),
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea")
		);
	}


	@Test
	void testPageRenameListener(FxRobot robot)
	{
		showMainView();

		// Check precondition
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 1", "Page 2");

		// Perform action
		eventHandler.fireEvent(new PageRenameUpdate(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), "Renamed Page"
		));
		WaitForAsyncUtils.waitForFxEvents();

		// Verify
		assertThat(robot.lookup("." + ViewConstants.PAGE_BUTTON_STYLECLASS).queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Renamed Page", "Page 2");
	}
}
