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
import de.tobias.playwall.common.api.page.update.PageAddUpdate;
import de.tobias.playwall.common.api.page.update.PageReorderUpdate;
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

	@Test
	void testPageAddListener(FxRobot robot)
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(robot.lookup(".page-button").queryAll()).hasSize(2);

		eventHandler.fireEvent(new PageAddUpdate(new PageDto(UUID.randomUUID(), "Page 3", 1,
				IntStream.range(0, project.getMetadata().getNumberOfPadsPerPage())
						.mapToObj(i -> new PadDto(UUID.randomUUID(), i, null, null, null, Color.GRAY1, Color.RED3)).toList())));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(robot.lookup(".page-button").queryAll()).hasSize(3);
		assertThat(robot.lookup(".page-button").queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactlyInAnyOrder("Page 1", "Page 2", "Page 3");
		assertThat(robot.lookup(".page-button").queryAll()).last().satisfies(button -> assertThat(((Button) button).getStyleClass()).contains(ViewConstants.PAGE_BUTTON_CURRENT_STYLECLASS));
	}

	@Test
	void testPageReorderListener(FxRobot robot)
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(robot.lookup(".page-button").queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 1", "Page 2");
		assertThat(project.getPages()).extracting(Page::getId).containsExactly(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"),
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e")
		);

		eventHandler.fireEvent(new PageReorderUpdate(Map.of(
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 1,
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e"), 0
		)));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(robot.lookup(".page-button").queryAll())
				.extracting(node -> ((Button) node).getText())
				.containsExactly("Page 2", "Page 1");
		assertThat(project.getPages()).extracting(Page::getId).containsExactly(
				UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e"),
				UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea")
		);
	}
}
