package de.tobias.playwall.client.viewcontroller;

import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.client.net.Client;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.testfx.assertions.api.Assertions.assertThat;

class LaunchViewControllerTest extends AbstractViewControllerTest
{
	private Client client;
	private LaunchDialog launchDialog;

	private static final UUID PROJECT_ID = UUID.randomUUID();
	public static final ProjectMetadata PROJECT_METADATA_1 = new ProjectMetadata(PROJECT_ID, "Test 1", 6, 4);

	@Start
	private void start(Stage stage)
	{
		final AppContext context = AppContextHolder.getInstance();
		context.registerLazySingleton(Stage.class, _ -> stage);
		context.registerLazySingleton(Client.class, _ -> mock(Client.class));
		client = context.get(Client.class);

		launchDialog = context.get(LaunchDialog.class);
		stage.show();
	}

	@Test
	void testProjectListDisplayAllProjects() throws Exception
	{
		when(client.getProjects()).thenReturn(List.of(new ProjectMetadata(PROJECT_ID, "Test 1", 6, 4)));
		launchDialog.fetchProjects();
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(launchDialog.getProjectListView()).hasExactlyNumItems(1);
	}

	@Test
	void testProjectListDisplayPlaceholder() throws Exception
	{
		when(client.getProjects()).thenReturn(List.of());
		launchDialog.fetchProjects();
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(launchDialog.getProjectListView()).hasExactlyNumItems(0);
	}

	@Test
	void testProjectDelete(FxRobot robot) throws Exception
	{
		when(client.getProjects()).thenReturn(List.of(PROJECT_METADATA_1));
		launchDialog.fetchProjects();
		WaitForAsyncUtils.waitForFxEvents();

		final Node cell = launchDialog.getProjectListView().lookupAll(".cell").toArray(Node[]::new)[0];
		robot.clickOn(cell, Motion.DEFAULT);
		robot.clickOn(launchDialog.getDeleteButton());
		robot.clickOn(robot.lookup("OK").lookup(".button").queryButton());
		verify(client).deleteProject(PROJECT_ID);
	}
}
