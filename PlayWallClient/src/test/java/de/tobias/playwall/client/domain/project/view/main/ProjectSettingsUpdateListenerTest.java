package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.testfx.assertions.api.Assertions.assertThat;

class ProjectSettingsUpdateListenerTest extends AbstractViewControllerTest
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
	void testProjectSettingsUpdateListener()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mainViewController.getProjectTitleLabel().getText()).isEqualTo("PlayWall - Project 1");

		eventHandler.fireEvent(new ProjectSettingsUpdate(ProjectMetadataDto.builder()
				.name("Fancy project name")
				.defaultColor(Color.GRAY4)
				.playColor(Color.RED3)
				.introColor(Color.LIGHT_GREEN1)
				.volume(1.0)
				.mappings(Map.of(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"), new JsonMapper().createArrayNode()))
				.selectedMapping(UUID.fromString("b9a0949e-1006-4a91-a755-c2b2e5539013"))
				.build()));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(mainViewController.getProjectTitleLabel().getText()).isEqualTo("PlayWall - Fancy project name");
	}
}
