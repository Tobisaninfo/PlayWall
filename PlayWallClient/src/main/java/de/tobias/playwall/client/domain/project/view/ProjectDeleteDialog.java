package de.tobias.playwall.client.domain.project.view;

import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Window;

import static de.thecodelabs.utils.util.Localization.getString;

public class ProjectDeleteDialog extends Alert
{
	public ProjectDeleteDialog(ProjectMetadata project, Window owner)
	{
		super(AlertType.CONFIRMATION);

		setTitle(getString(Strings.UI_DIALOG_PROJECT_DELETE_TITLE, project.getName()));
		setContentText(getString(Strings.UI_DIALOG_PROJECT_DELETE_CONTENT, project.getName()));
		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);
		getDialogPane().setMinHeight(Double.NEGATIVE_INFINITY);
	}
}
