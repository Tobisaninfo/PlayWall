package de.tobias.playwall.client.domain.project.view;

import de.tobias.playwall.client.AbstractViewControllerTest;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProjectNewDialogTest extends AbstractViewControllerTest
{
	private AppContext context;
	private final Client client = mock(Client.class);

	private ProjectNewDialog projectNewDialog;
	private Stage stage;

	@Start
	private void start(Stage stage)
	{
		this.stage = stage;
		context = AppContextHolder.getInstance();
		context.registerLazy(Stage.class, _ -> stage);

		context.registerLazySingleton(Client.class, _ -> client);
	}

	@Test
	void testCreateProjectOkay(FxRobot robot) throws PlayWallApiException
	{
		final ProjectMetadata metadata = new ProjectMetadata(UUID.randomUUID(), "Test", 5, 3, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3, ModernColor.LIGHT_GREEN2, null);
		when(client.addProject(any(), anyInt(), anyInt())).thenReturn(metadata);

		Platform.runLater(() -> {
			projectNewDialog = context.get(ProjectNewDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		Platform.runLater(() -> {
			projectNewDialog.getTextFieldName().setText("Test");
			projectNewDialog.getSpinnerNumberOfHorizontalPads().getValueFactory().setValue(5);
			projectNewDialog.getSpinnerNumberOfVerticalPads().getValueFactory().setValue(3);
		});

		robot.clickOn(projectNewDialog.getSettingsPage().getSaveButton());

		verify(client).addProject("Test", 5, 3);
		assertThat(stage.isShowing()).isFalse();
		assertThat(projectNewDialog.getResultValue()).isEqualTo(metadata);
	}

	@Test
	void testCreateProjectNameDuplicate(FxRobot robot) throws PlayWallApiException
	{
		when(client.addProject(any(), anyInt(), anyInt())).thenThrow(new PlayWallApiException("Das Projekt mit dem Namen \"Test\" konnte nicht angelegt werden. Es existiert bereits ein Projekt mit diesem Namen.", new ProjectNameAlreadyExistsError("Test")));

		Platform.runLater(() -> {
			projectNewDialog = context.get(ProjectNewDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		Platform.runLater(() -> {
			projectNewDialog.getTextFieldName().setText("Test");
			projectNewDialog.getSpinnerNumberOfHorizontalPads().getValueFactory().setValue(5);
			projectNewDialog.getSpinnerNumberOfVerticalPads().getValueFactory().setValue(3);
		});

		robot.clickOn(projectNewDialog.getSettingsPage().getSaveButton());

		verify(client).addProject("Test", 5, 3);
		assertThat(stage.isShowing()).isTrue();

		Assertions.assertThat(robot.lookup(".label.content").queryLabeled()).hasText("Das Projekt mit dem Namen \"Test\" konnte nicht angelegt werden. Es existiert bereits ein Projekt mit diesem Namen.");
	}

	@Test
	void testCreateProjectCanceled(FxRobot robot)
	{
		Platform.runLater(() -> {
			projectNewDialog = context.get(ProjectNewDialog.class);
			stage.show();
		});
		WaitForAsyncUtils.waitForFxEvents();

		robot.clickOn(projectNewDialog.getSettingsPage().getCancelButton());
		assertThat(projectNewDialog.getResultValue()).isNull();
	}
}
