package de.tobias.playwall.client.view.toast;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.function.Consumer;

import static de.tobias.playwall.client.view.toast.MaterialToastManager.TOAST_WIDTH;

public class Toast extends VBox
{
	public Toast(String title, String message, ToastType type, Consumer<Node> onClose)
	{

		final Region accent = new Region();
		accent.setPrefWidth(7);
		accent.setMinWidth(7);
		accent.getStyleClass().addAll("accent", type.name().toLowerCase());

		final FontIcon iconLabel = new FontIcon(type.getIcon());

		final StackPane iconCircle = new StackPane(iconLabel);
		iconCircle.setMaxSize(28, 28);
		iconCircle.setPrefSize(28, 28);
		iconCircle.setMinSize(28, 28);
		iconCircle.getStyleClass().addAll("icon-circle", type.name().toLowerCase());
		StackPane.setAlignment(iconLabel, Pos.CENTER);

		final Label titleLabel = new Label(title);
		titleLabel.getStyleClass().add("title");

		final Label messageLabel = new Label(message);
		messageLabel.getStyleClass().add("message");
		messageLabel.setWrapText(true);
		messageLabel.setMaxWidth(210);
		messageLabel.setMinHeight(Region.USE_PREF_SIZE);

		final FontIcon closeIcon = new FontIcon(FontAwesomeType.XMARK_SOLID);
		closeIcon.getStyleClass().add("close-button");

		final VBox textBox = new VBox(3, titleLabel, messageLabel);
		textBox.setAlignment(Pos.TOP_LEFT);
		VBox.setVgrow(messageLabel, Priority.ALWAYS);

		final HBox content = new HBox(ViewConstants.DEFAULT_SPACING, iconCircle, textBox);
		content.setAlignment(Pos.CENTER_LEFT);
		content.setPadding(new Insets(ViewConstants.DEFAULT_SPACING, 0, ViewConstants.DEFAULT_SPACING, ViewConstants.DEFAULT_SPACING));
		HBox.setHgrow(textBox, Priority.ALWAYS);

		final HBox row = new HBox(content, closeIcon);
		row.setAlignment(Pos.TOP_RIGHT);
		HBox.setHgrow(content, Priority.ALWAYS);
		HBox.setMargin(closeIcon, new Insets(8, 8, 0, 0));

		final HBox outer = new HBox(accent, row);
		outer.setAlignment(Pos.TOP_LEFT);
		HBox.setHgrow(row, Priority.ALWAYS);
		HBox.setHgrow(accent, Priority.NEVER);

		getChildren().add(outer);
		getStyleClass().add("toast");
		setPrefWidth(TOAST_WIDTH);
		setMaxWidth(TOAST_WIDTH);
		setMaxHeight(Region.USE_COMPUTED_SIZE);

		closeIcon.setOnMouseClicked(_ -> onClose.accept(this));
	}
}
