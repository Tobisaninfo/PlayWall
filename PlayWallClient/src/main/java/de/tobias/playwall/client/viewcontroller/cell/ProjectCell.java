package de.tobias.playwall.client.viewcontroller.cell;

import de.tobias.playwall.client.model.project.ProjectMetadata;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ProjectCell extends ListCell<ProjectMetadata>
{
	private ProjectMetadata ref;

	@Override
	protected void updateItem(ProjectMetadata ref, boolean empty)
	{
		super.updateItem(ref, empty);
		if(!empty)
		{
			if(this.ref == null || this.ref != ref)
			{
				HBox rootBox = new HBox(14);
				VBox nameBox = new VBox(3);

				// init
				rootBox.setAlignment(Pos.CENTER_LEFT);

				// Project Name
				Label projectNameLabel = new Label();
				projectNameLabel.textProperty().setValue(ref.name());
				projectNameLabel.getStyleClass().add("launch-dialog--project-name");
				nameBox.getChildren().add(projectNameLabel);

				HBox.setHgrow(nameBox, Priority.ALWAYS);
				rootBox.getChildren().add(nameBox);

				// File not Exists
				// TODO: Error Handling
//				Path path = ref.getProjectPath();
//				if((Files.notExists(path) || !ref.getMissedModules().isEmpty()))
//				{
//					FontIcon graphics = new FontIcon(FontAwesomeType.WARNING);
//					graphics.setColor(Color.RED);
//					rootBox.getChildren().add(graphics);
//				}

				setGraphic(rootBox);
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
