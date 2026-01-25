package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.mockito.Mockito.mock;
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
		assertThat(mainViewController.getPageButtonsFlowPane().getChildren().getFirst().getStyleClass()).contains(ViewConstants.PAGE_BUTTON_CURRENT_STYLECLASS);
		assertThat(mainViewController.getPageButtonsFlowPane().getChildren().get(1).getStyleClass()).doesNotContain(ViewConstants.PAGE_BUTTON_CURRENT_STYLECLASS);

		// Assert current page
		final DesktopPadView padView1 = (DesktopPadView) mainViewController.getPadViewForPosition(0);
		assertThat(padView1.getNamePreviewLabel()).hasText("Test Pad");

		// Switch page
		robot.clickOn(mainViewController.getPageButtonsFlowPane().getChildren().get(1));
		WaitForAsyncUtils.waitForFxEvents();

		// Assert highlighting
		assertThat(mainViewController.getPageButtonsFlowPane().getChildren().getFirst().getStyleClass()).doesNotContain(ViewConstants.PAGE_BUTTON_CURRENT_STYLECLASS);
		assertThat(mainViewController.getPageButtonsFlowPane().getChildren().get(1).getStyleClass()).contains(ViewConstants.PAGE_BUTTON_CURRENT_STYLECLASS);

		// Assert current page
		final DesktopPadView padView2 = (DesktopPadView) mainViewController.getPadViewForPosition(0);
		assertThat(padView2.getNamePreviewLabel().getText()).isEmpty();
	}
}
