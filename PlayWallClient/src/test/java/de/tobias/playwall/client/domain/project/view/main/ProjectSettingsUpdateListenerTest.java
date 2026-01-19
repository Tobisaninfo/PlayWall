package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.testfx.assertions.api.Assertions.assertThat;

class ProjectSettingsUpdateListenerTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private UpdateMessageEventHandler eventHandler;

	private Project project;

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		final FluentClient client = Mockito.mock(FluentClient.class);
		Mockito.when(client.currentProject()).thenReturn(Mockito.mock(FluentClient.ProjectCurrentBuilder.class));
		context.registerLazy(FluentClient.class, _ -> client);

		eventHandler = context.get(UpdateMessageEventHandler.class);

		project = loadProject("projects/project_1.json");
		ClientProjectController projectController = context.get(ClientProjectController.class);
		projectController.loadProject(project);
	}

	@Test
	void testProjectSettingsUpdateListener()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mainViewController.getProjectTitleLabel().getText()).isEqualTo("PlayWall - Project 1");

		eventHandler.fireEvent(new ProjectSettingsUpdate(ProjectMetadataDto.builder()
				.name("Fancy project name")
				.build()));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mainViewController.getProjectTitleLabel().getText()).isEqualTo("PlayWall - Fancy project name");
	}
}
