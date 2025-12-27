package de.tobias.playwall.iconoverview;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

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
			fontIcon.setMinWidth(50);
			fontIcon.setPrefWidth(50);

			final Label labelName = new Label();
			labelName.setText(ref.fontIconType().toString());
			labelName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
			labelName.setMinWidth(250);
			labelName.setPrefWidth(250);

			final Label labelDescription = new Label();
			if(ref.isDeclared() && ref.description() != null)
			{
				labelDescription.setText(ref.description());
			}
			labelDescription.setStyle("-fx-font-size: 12px;");
			labelDescription.setMinWidth(250);
			labelDescription.setPrefWidth(250);

			rootBox.getChildren().addAll(fontIcon, labelName, labelDescription);


			if(!ref.isDeclared())
			{
				final FontIcon warningIcon = new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID);
				warningIcon.setColor(Color.web("#FF0000"));
				warningIcon.setSize(15);
				warningIcon.setMinWidth(25);
				warningIcon.setPrefWidth(25);

				final Label labelWarning = new Label();
				labelWarning.setText("Nicht deklariert");
				labelWarning.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #FF0000;");
				labelWarning.setMinWidth(100);
				labelWarning.setPrefWidth(100);

				rootBox.getChildren().addAll(warningIcon, labelWarning);
			}

			setGraphic(rootBox);

			this.ref = ref;
		}
	}
}
