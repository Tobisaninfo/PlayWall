package de.tobias.playwall.client.domain.project.view.management;

import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.extensions.AppEnvironmentSetup;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.shape.Circle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(AppEnvironmentSetup.class)
class ProjectManagementCellTest extends ApplicationTest
{
	private static final UUID PROJECT_ID_1 = UUID.randomUUID();
	private static final ProjectMetadata PROJECT_METADATA_1 = new ProjectMetadata(PROJECT_ID_1, "Test 1", 6, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3, ModernColor.LIGHT_GREEN2, null);
	private static final UUID PROJECT_ID_2 = UUID.randomUUID();
	private static final ProjectMetadata PROJECT_METADATA_2 = new ProjectMetadata(PROJECT_ID_2, "Test 2", 6, 4, 1.0, TimeMode.ELAPSED, ModernColor.GRAY1, ModernColor.RED3, ModernColor.LIGHT_GREEN2, null);

	private final ClientProjectController projectController = mock(ClientProjectController.class);
	private final BiConsumer<ProjectManagementCell.ProjectManagementCellAction, ProjectMetadata> onContextMenuAction = mock(BiConsumer.class);

	@Test
	void testCellWithActiveProject()
	{
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_1, List.of()));

		final ProjectManagementCell cell = new ProjectManagementCell(projectController, onContextMenuAction);

		interact(() -> cell.updateItem(PROJECT_METADATA_1, false));
		WaitForAsyncUtils.waitForFxEvents();

		final Label label = (Label) cell.getGraphic().lookup(".project-management--project-name");
		final Circle circle = (Circle) cell.getGraphic().lookup(".project-management--circle");
		final Button menuButton = (Button) cell.getGraphic().lookup(".button");

		assertThat(label).hasText("Test 1");
		assertThat(circle).isVisible();

		assertThat(menuButton).isVisible();

		assertThat(cell.getButtonContextMenu().getItems()).extracting(MenuItem::getText)
				.containsExactly("Umbenennen", "Duplizieren", "Exportieren", null, "Löschen");
	}

	@Test
	void testCellWithInactiveProject()
	{
		when(projectController.getProject()).thenReturn(new Project(PROJECT_METADATA_2, List.of()));

		final ProjectManagementCell cell = new ProjectManagementCell(projectController, onContextMenuAction);

		interact(() -> cell.updateItem(PROJECT_METADATA_1, false));
		WaitForAsyncUtils.waitForFxEvents();

		final Label label = (Label) cell.getGraphic().lookup(".project-management--project-name");
		final Circle circle = (Circle) cell.getGraphic().lookup(".project-management--circle");

		assertThat(label).hasText("Test 1");
		assertThat(circle).isInvisible();
	}

	@Test
	void testCellEmptyState()
	{
		final ProjectManagementCell cell = new ProjectManagementCell(projectController, onContextMenuAction);

		interact(() -> cell.updateItem(null, true));
		WaitForAsyncUtils.waitForFxEvents();

		assertThat(cell.getGraphic()).isNull();
	}
}
