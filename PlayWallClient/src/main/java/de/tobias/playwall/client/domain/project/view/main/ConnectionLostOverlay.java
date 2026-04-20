package de.tobias.playwall.client.domain.project.view.main;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ConnectionLostOverlay extends StackPane
{
	public ConnectionLostOverlay(Runnable onClose)
	{
		final Region background = new Region();
		background.getStyleClass().add("background");
		background.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		background.addEventFilter(MouseEvent.ANY, Event::consume);

		final Region errorStripe = new Region();
		errorStripe.getStyleClass().add("accent");
		errorStripe.setPrefWidth(6);

		final Label iconLabel = new FontIcon(FontAwesomeType.XMARK_SOLID);
		final Label titleLabel = new Label(Localization.getString(Strings.UI_CONNECTION_LOST_TITLE));
		titleLabel.getStyleClass().add("title");

		final HBox titleRow = new HBox(10, iconLabel, titleLabel);
		titleRow.setAlignment(Pos.CENTER_LEFT);

		final Label descriptionLabel = new Label(Localization.getString(Strings.UI_CONNECTION_LOST_DESCRIPTION));
		descriptionLabel.getStyleClass().add("description");
		descriptionLabel.setWrapText(true);
		descriptionLabel.setMaxWidth(380);

		final Region separator = new Region();
		separator.getStyleClass().add("separator");
		separator.setPrefHeight(1);
		separator.setMaxWidth(Double.MAX_VALUE);

		final Button closeButton = new Button(Localization.getString(Strings.UI_CONNECTION_LOST_BUTTON_CLOSE));
		closeButton.getStyleClass().add("danger");
		closeButton.setOnAction(_ -> onClose.run());

		final HBox buttonBar = new HBox(closeButton);
		buttonBar.setAlignment(Pos.CENTER_RIGHT);
		buttonBar.setPadding(new Insets(4, 0, 0, 0));

		final VBox body = new VBox(12, titleRow, descriptionLabel, separator, buttonBar);
		body.setPadding(new Insets(16));

		final HBox contentBox = new HBox(errorStripe, body);
		contentBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		contentBox.setMinWidth(420);
		contentBox.getStyleClass().add("content");

		getChildren().addAll(background, contentBox);
		getStyleClass().add("connection-lost-overlay");

		addEventFilter(KeyEvent.ANY, Event::consume);

		setVisible(false);
	}
}
