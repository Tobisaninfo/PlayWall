package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.FileChooserWrapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.*;

class MainViewControllerPadNewFileTest extends AbstractViewControllerTest
{
	private AppContext context;

	private MainViewController mainViewController;
	private Stage stage;

	private Project project;

	private final FileChooserWrapper fileChooserWrapper = mock(FileChooserWrapper.class);
	private final Client client = mock(Client.class);

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(FileChooserWrapper.class, _ -> fileChooserWrapper);
		context.registerLazySingleton(Client.class, _ -> client);

		project = loadProject("projects/project_1.json");
		ClientProjectController projectController = context.get(ClientProjectController.class);
		projectController.loadProject(project);
	}

	@Test
	void testSelectNewMediaSuccessful(FxRobot robot) throws URISyntaxException, PlayWallApiException
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final Path path = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI());
		when(fileChooserWrapper.showOpenFile(any())).thenReturn(Optional.of(path));
		when(fileChooserWrapper.showByActionEvent(any(ActionEvent.class))).thenReturn(Optional.of(path));

		final UUID padId = UUID.fromString("57accabc-7d19-473c-a0a1-ea5c61b85e18");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		robot.clickOn(padView.getNewButton());

		verify(client).newMedia(padId, path);
	}

	@Test
	void testSelectNewMediaCancel(FxRobot robot) throws PlayWallApiException
	{
		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.showProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		when(fileChooserWrapper.showOpenFile(any())).thenReturn(Optional.empty());

		final UUID padId = UUID.fromString("57accabc-7d19-473c-a0a1-ea5c61b85e18");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		robot.clickOn(padView.getNewButton());

		verify(client, never()).newMedia(any(), any());
	}
}
