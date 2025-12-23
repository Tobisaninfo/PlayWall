package de.tobias.playwall.iconoverview;

import de.thecodelabs.utils.ui.icon.FontIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

public class IconCell extends ListCell<IconEntry>
{
	private IconEntry ref;

	@Override
	protected void updateItem(IconEntry ref, boolean empty)
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

		if(this.ref == null || this.ref != ref)
		{
			final HBox rootBox = new HBox(14);

			rootBox.setAlignment(Pos.CENTER_LEFT);

			final FontIcon fontIcon = new FontIcon(ref.fontIconType());
			fontIcon.setSize(30);

			final Label labelName = new Label();
			labelName.textProperty().setValue(ref.fontIconType().toString());
			labelName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
			labelName.setMinWidth(250);

			final Label labelDescription = new Label();
			labelDescription.textProperty().setValue(ref.description());
			labelDescription.setStyle("-fx-font-size: 12px;");
			labelDescription.setMinWidth(250);

			rootBox.getChildren().addAll(fontIcon, labelName, labelDescription);

			setGraphic(rootBox);

			this.ref = ref;
		}
	}
}
