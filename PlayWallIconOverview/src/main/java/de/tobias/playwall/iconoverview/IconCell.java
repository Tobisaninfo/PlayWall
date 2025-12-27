package de.tobias.playwall.iconoverview;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class IconCell extends ListCell<IconEntry>
{
	private IconEntry ref;
	private Label labelName = new Label();

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

			labelName.setText(ref.fontIconType().toString());
			labelName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
			labelName.setMinWidth(250);
			labelName.setPrefWidth(250);

			final VBox boxAllUsages = new VBox(14);
			boxAllUsages.setAlignment(Pos.CENTER_LEFT);

			for(IconUsage usage : ref.usages())
			{
				final HBox boxUsage = new HBox(14);
				boxUsage.setAlignment(Pos.CENTER_LEFT);

				final Label labelCategory = new Label();
				labelCategory.setText(usage.category().getName());
				labelCategory.setStyle("-fx-font-size: 12px; -fx-text-fill: " + usage.category().getFontColor() + "; -fx-background-radius: 3px; -fx-background-color: " + usage.category().getBackgroundColor());
				labelCategory.setPadding(new Insets(3, 6, 3, 6));
				boxUsage.getChildren().add(labelCategory);

				final Label labelDescription = new Label();
				labelDescription.setText(usage.description());
				labelDescription.setStyle("-fx-font-size: 12px;");
				boxUsage.getChildren().add(labelDescription);

				boxAllUsages.getChildren().add(boxUsage);
			}
			HBox.setHgrow(boxAllUsages, Priority.ALWAYS);

			rootBox.getChildren().addAll(fontIcon, labelName, boxAllUsages);

			if(ref.usages().isEmpty())
			{
				final HBox box = new HBox(7);
				box.setAlignment(Pos.CENTER_LEFT);

				final FontIcon warningIcon = new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID);
				warningIcon.setColor(Color.web("#FF0000"));
				warningIcon.setSize(15);
				warningIcon.setMinWidth(15);
				warningIcon.setPrefWidth(15);

				final Label labelWarning = new Label();
				labelWarning.setText("Nicht deklariert");
				labelWarning.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #FF0000;");
				labelWarning.setMinWidth(100);
				labelWarning.setPrefWidth(100);

				box.getChildren().addAll(warningIcon, labelWarning);
				boxAllUsages.getChildren().add(box);
			}

			setGraphic(rootBox);

			this.ref = ref;
		}
	}

	public void onDoubleClick()
	{
		if(ref == null)
		{
			return;
		}

		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		content.putString(ref.fontIconType().toString());
		clipboard.setContent(content);

		final String originalText = labelName.getText();

		labelName.setText("Kopiert");
		final PauseTransition delay = new PauseTransition(Duration.seconds(1));
		delay.setOnFinished(_ -> labelName.setText(originalText));
		delay.play();
	}
}
