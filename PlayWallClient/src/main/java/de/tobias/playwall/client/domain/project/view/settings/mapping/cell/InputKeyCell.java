package de.tobias.playwall.client.domain.project.view.settings.mapping.cell;

import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.input.InputKey;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.domain.mapping.action.ActionStringify;
import de.tobias.playwall.client.domain.mapping.action.ActionValidation;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.view.settings.InputKeyLocalizer;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;
import java.util.function.Function;

public class InputKeyCell extends ListCell<InputKey>
{
	private static final double SCROLLBAR_RESERVE = 25.0;

	private final Project project;
	private final Function<InputKey, Action> actionProvider;

	final HBox hbox;
	final VBox vbox;
	final FontIcon icon;
	final Label keyLabel;
	final Label actionLabel;
	final PlayWallButton buttonDelete;
	final Consumer<InputKey> deleteActionHandler;

	public InputKeyCell(Project project, Function<InputKey, Action> actionProvider, Consumer<InputKey> deleteActionHandler)
	{
		this.project = project;
		this.actionProvider = actionProvider;
		this.deleteActionHandler = deleteActionHandler;

		icon = new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID);
		icon.getStyleClass().add("warning");
		icon.managedProperty().bind(icon.visibleProperty());

		hbox = new HBox(ViewConstants.DEFAULT_SPACING);
		vbox = new VBox(4);
		keyLabel = new Label();
		actionLabel = new Label();


		vbox.getChildren().addAll(keyLabel, actionLabel);

		buttonDelete = new PlayWallButton(FontAwesomeType.TRASH_CAN_SOLID);

		hbox.getChildren().addAll(vbox, icon, buttonDelete);
		hbox.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(vbox, Priority.ALWAYS);

		setMaxWidth(Region.USE_PREF_SIZE);
		listViewProperty().addListener((_, _, newListView) ->
		{
			prefWidthProperty().unbind();
			if(newListView != null)
			{
				prefWidthProperty().bind(newListView.widthProperty().subtract(SCROLLBAR_RESERVE));
			}
		});
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
			icon.setVisible(false);
			return;
		}

		keyLabel.setText(InputKeyLocalizer.localize(item));

		final Action action = actionProvider.apply(item);
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

		if(action instanceof ActionValidation validation)
		{
			icon.setVisible(!validation.isValid(project));
		}
		else
		{
			icon.setVisible(false);
		}

		buttonDelete.setVisible(true);
		buttonDelete.setOnAction(_ -> deleteActionHandler.accept(item));
	}
}
