package de.tobias.playwall.client.domain.project.view.settings.cell;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.input.InputKey;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.tobias.playwall.client.domain.mapping.action.ActionStringify;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.view.settings.InputKeyLocalizer;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class InputKeyCell extends ListCell<InputKey>
{
	private final Project project;
	private final Mapping mapping;

	final HBox hbox;
	final VBox vbox;
	final Label keyLabel;
	final Label actionLabel;
	final PlayWallButton buttonDelete;
	final Consumer<InputKey> deleteActionHandler;

	public InputKeyCell(Project project, Mapping mapping, Consumer<InputKey> deleteActionHandler)
	{
		this.project = project;
		this.mapping = mapping;
		this.deleteActionHandler = deleteActionHandler;

		hbox = new HBox(ViewConstants.DEFAULT_SPACING);
		vbox = new VBox(4);
		keyLabel = new Label();
		actionLabel = new Label();

		vbox.getChildren().addAll(keyLabel, actionLabel);

		buttonDelete = new PlayWallButton(FontAwesomeType.TRASH_CAN_SOLID);

		hbox.getChildren().addAll(vbox, buttonDelete);
		hbox.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(vbox, Priority.ALWAYS);
	}

	@Override
	protected void updateItem(InputKey item, boolean empty)
	{
		super.updateItem(item, empty);
		setGraphic(hbox);

		if(empty || item == null)
		{
			keyLabel.setText("");
			actionLabel.setText("");
			buttonDelete.setVisible(false);
			return;
		}

		keyLabel.setText(InputKeyLocalizer.localize(item));

		final Action action = mapping.getAction(item);
		if(action == null)
		{
			actionLabel.setText("<Leer>");
		}
		else if(action instanceof ActionStringify stringify)
		{
			actionLabel.setText(stringify.stringify(project));
		}
		else
		{
			actionLabel.setText(action.toString());
		}

		buttonDelete.setVisible(true);
		buttonDelete.setOnAction(_ -> deleteActionHandler.accept(item));
	}
}
