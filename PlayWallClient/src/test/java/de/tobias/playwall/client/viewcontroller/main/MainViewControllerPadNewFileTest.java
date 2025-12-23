package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.viewcontroller.AbstractViewControllerTest;
import de.tobias.playwall.client.viewcontroller.FileChooserWrapper;
import de.tobias.playwall.client.viewcontroller.main.desktop.DesktopPadView;
import javafx.application.Platform;
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
	}

	@Test
	void testSelectNewMediaSuccessful(FxRobot robot) throws URISyntaxException, PlayWallApiException
	{
		final AboutDialog dialog = mock(AboutDialog.class);
		AppContextHolder.getInstance().registerLazySingleton(AboutDialog.class, _ -> dialog);

		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.openProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		final Path path = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI());
		when(fileChooserWrapper.showOpenFile(any())).thenReturn(Optional.of(path));

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		robot.clickOn(padView.getNewButton());

		verify(client).newMedia(padId, path);
	}

	@Test
	void testSelectNewMediaCancel(FxRobot robot) throws PlayWallApiException
	{
		final AboutDialog dialog = mock(AboutDialog.class);
		AppContextHolder.getInstance().registerLazySingleton(AboutDialog.class, _ -> dialog);

		Platform.runLater(() -> {
			mainViewController = context.get(MainViewController.class);
			mainViewController.openProject(project);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		when(fileChooserWrapper.showOpenFile(any())).thenReturn(Optional.empty());

		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final DesktopPadView padView = (DesktopPadView) mainViewController.getPadViewForPadId(padId);
		robot.clickOn(padView.getNewButton());

		verify(client, never()).newMedia(any(), any());
	}
}
