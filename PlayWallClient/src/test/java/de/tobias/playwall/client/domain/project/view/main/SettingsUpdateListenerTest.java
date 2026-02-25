package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.common.api.settings.model.SettingsDto;
import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import de.tobias.playwall.common.api.settings.update.SettingsUpdate;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.mockito.Mockito.mock;
import static org.testfx.assertions.api.Assertions.assertThat;

class SettingsUpdateListenerTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private UpdateMessageEventHandler eventHandler;

	private Project project;
	private ClientSettingsController settingsController;

	private final Client client = mock(Client.class);

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);

		eventHandler = context.get(UpdateMessageEventHandler.class);
		settingsController = context.get(ClientSettingsController.class);

		project = loadProject("projects/project_1.json");
	}

	@Test
	void testSettingsUpdateListener()
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			mainViewController.showLoadingOverlay(false);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(settingsController.getSettings()).isNull();

		eventHandler.fireEvent(new SettingsUpdate(SettingsDto.builder()
				.autoLoadLatestProjectOnStart(true)
				.unsavedChangesMode(UnsavedChangesMode.DISCARD)
				.build()));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(settingsController.getSettings().isAutoLoadLatestProjectOnStart()).isTrue();
		assertThat(settingsController.getSettings().getUnsavedChangesMode()).isEqualTo(UnsavedChangesMode.DISCARD);
	}
}
