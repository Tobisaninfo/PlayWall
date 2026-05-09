package de.tobias.playwall.client.domain.project.view.management;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.Getter;

import java.util.Objects;
import java.util.function.BiConsumer;

public class ProjectManagementCell extends ListCell<ProjectMetadata>
{
	public enum ProjectManagementCellAction
	{
		RENAME,
		DUPLICATE,
		EXPORT,
		DELETE
	}

	private final ClientProjectController projectController;
	@Getter
	private final ContextMenu buttonContextMenu;

	private ProjectMetadata ref;

	ProjectManagementCell(ClientProjectController projectController,
	                      BiConsumer<ProjectManagementCellAction, ProjectMetadata> actionHandler)
	{
		this.projectController = projectController;
		buttonContextMenu = new ContextMenu();

		final MenuItem renameMenu = new MenuItem(Localization.getString(Strings.UI_PROJECT_BUTTON_RENAME), new FontIcon(FontAwesomeType.PEN_SOLID));
		final MenuItem duplicateMenu = new MenuItem(Localization.getString(Strings.UI_PROJECT_BUTTON_DUPLICATE), new FontIcon(FontAwesomeType.CLONE_SOLID));
		final MenuItem exportMenu = new MenuItem(Localization.getString(Strings.UI_PROJECT_BUTTON_EXPORT), new FontIcon(FontAwesomeType.FILE_IMPORT_SOLID));
		final MenuItem deleteMenu = new MenuItem(Localization.getString(Strings.UI_PROJECT_BUTTON_DELETE), new FontIcon(FontAwesomeType.TRASH_CAN_SOLID));
		deleteMenu.getStyleClass().add("danger");

		renameMenu.setOnAction(_ -> {
			if(ref != null)
			{
				actionHandler.accept(ProjectManagementCellAction.RENAME, ref);
			}
		});
		duplicateMenu.setOnAction(_ -> {
			if(ref != null)
			{
				actionHandler.accept(ProjectManagementCellAction.DUPLICATE, ref);
			}
		});
		exportMenu.setOnAction(_ -> {
			if(ref != null)
			{
				actionHandler.accept(ProjectManagementCellAction.EXPORT, ref);
			}
		});
		deleteMenu.setOnAction(_ -> {
			if(ref != null)
			{
				actionHandler.accept(ProjectManagementCellAction.DELETE, ref);
			}
		});

		buttonContextMenu.getItems().addAll(renameMenu, duplicateMenu, exportMenu, new SeparatorMenuItem(), deleteMenu);
	}

	@Override
	protected void updateItem(ProjectMetadata ref, boolean empty)
	{
		super.updateItem(ref, empty);
		if(empty)
		{
			this.ref = null;
			textProperty().unbind();

			setGraphic(null);
			setText("");

			return;
		}

		// already up to date
		if(this.ref != null && this.ref == ref)
		{
			return;
		}

		final HBox row = new HBox();
		row.setAlignment(Pos.CENTER_LEFT);
		row.setSpacing(ViewConstants.DEFAULT_SPACING);
		row.setPadding(new Insets(0, ViewConstants.DEFAULT_SPACING, 0, ViewConstants.DEFAULT_SPACING));

		final Circle circle = new Circle(6);
		circle.getStyleClass().add("project-management--circle");
		circle.setVisible(Objects.equals(projectController.getProject().getMetadata().getId(), ref.getId()));

		final Tooltip tooltip = new Tooltip(Localization.getString(Strings.UI_PROJECT_SELECTED_TOOLTIP));
		tooltip.setShowDelay(Duration.millis(300));
		Tooltip.install(circle, tooltip);

		final Label projectNameLabel = new Label();
		projectNameLabel.setMaxWidth(Double.MAX_VALUE);
		projectNameLabel.textProperty().setValue(ref.getName());
		projectNameLabel.getStyleClass().add("project-management--project-name");

		final Button menuButton = new Button("", new FontIcon(FontAwesomeType.ELLIPSIS_VERTICAL_SOLID));
		menuButton.setOnAction(_ -> buttonContextMenu.show(menuButton, Side.LEFT, 0, 0));

		row.getChildren().addAll(circle, projectNameLabel, menuButton);
		HBox.setHgrow(projectNameLabel, Priority.ALWAYS);

		setGraphic(row);
		this.ref = ref;
	}
}
