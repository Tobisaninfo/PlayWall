package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.net.Client;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.mockito.Mockito.mock;
import static org.testfx.assertions.api.Assertions.assertThat;

class MainViewControllerPadTest extends AbstractViewControllerTest
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
		ClientProjectController projectController = context.get(ClientProjectController.class);
		projectController.loadProject(project);
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
	void testPadContent()
	{
		showMainView();

		for(int i = 0; i < project.getPage(0).getPads().size(); i++)
		{
			final Pad pad = project.getPage(0).getPad(i);
			final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPosition(i);

			assertThat(padView.getIndexLabel().getText()).isEqualTo(pad.getReadablePosition());

			if(pad.getContent() != null)
			{
				assertThat(padView.getNamePreviewLabel().getText()).isEqualTo(pad.getName());

				assertThat(padView.getPlayButton()).isVisible();
				assertThat(padView.getPauseButton().getParent()).isNull();
				assertThat(padView.getStopButton()).isDisabled();
				assertThat(padView.getNewButton()).isVisible();
				assertThat(padView.getSettingsButton()).isVisible();
			}
			else
			{
				assertThat(padView.getNamePreviewLabel().getText()).isNullOrEmpty();

				assertThat(padView.getPlayButton().getParent()).isNull();
				assertThat(padView.getPauseButton().getParent()).isNull();
				assertThat(padView.getStopButton().getParent()).isNull();
				assertThat(padView.getNewButton()).isVisible();
				assertThat(padView.getSettingsButton()).isVisible();
			}
		}
	}
}
