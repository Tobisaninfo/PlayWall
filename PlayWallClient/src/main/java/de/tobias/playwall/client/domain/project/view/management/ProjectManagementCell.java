package de.tobias.playwall.client.domain.project.view.management;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
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

import java.util.Objects;

public class ProjectManagementCell extends ListCell<ProjectMetadata>
{
	private final ClientProjectController projectController;
	private final ContextMenu contextMenu;

	private ProjectMetadata ref;

	public ProjectManagementCell(ClientProjectController projectController)
	{
		this.projectController = projectController;
		contextMenu = new ContextMenu();

		final MenuItem renameMenu = new MenuItem(Localization.getString("project.button.rename"), new FontIcon(FontAwesomeType.PEN_SOLID));
		final MenuItem duplicateMenu = new MenuItem(Localization.getString("project.button.duplicate"), new FontIcon(FontAwesomeType.CLONE_SOLID));
		final MenuItem exportMenu = new MenuItem(Localization.getString("project.button.export"), new FontIcon(FontAwesomeType.FILE_IMPORT_SOLID));
		final MenuItem deleteMenu = new MenuItem(Localization.getString("project.button.delete"), new FontIcon(FontAwesomeType.TRASH_CAN_SOLID));

		contextMenu.getItems().addAll(renameMenu, duplicateMenu, exportMenu, new SeparatorMenuItem(), deleteMenu);
	}

	@Override
	protected void updateItem(ProjectMetadata ref, boolean empty)
	{
		super.updateItem(ref, empty);
		if(!empty)
		{
			if(this.ref == null || this.ref != ref)
			{
				final HBox row = new HBox();
				row.setAlignment(Pos.CENTER_LEFT);
				row.setSpacing(ViewConstants.DEFAULT_SPACING);
				row.setPadding(new Insets(0, ViewConstants.DEFAULT_SPACING, 0, ViewConstants.DEFAULT_SPACING));

				final Circle circle = new Circle(4);
				circle.getStyleClass().add("project-management--circle");
				circle.setVisible(Objects.equals(projectController.getProject().getMetadata().getId(), ref.getId()));

				final Label projectNameLabel = new Label();
				projectNameLabel.setMaxWidth(Double.MAX_VALUE);
				projectNameLabel.textProperty().setValue(ref.getName());
				projectNameLabel.getStyleClass().add("project-management--project-name");

				final Button menuButton = new Button("", new FontIcon(FontAwesomeType.ELLIPSIS_VERTICAL_SOLID));
				menuButton.setOnAction(e -> contextMenu.show(menuButton, Side.LEFT, 0, 0));

				row.getChildren().addAll(circle, projectNameLabel, menuButton);
				HBox.setHgrow(projectNameLabel, Priority.ALWAYS);

				setGraphic(row);
				this.ref = ref;
			}
		}
		else
		{
			this.ref = null;
			textProperty().unbind();

			setGraphic(null);
			setText("");
		}
	}
}
