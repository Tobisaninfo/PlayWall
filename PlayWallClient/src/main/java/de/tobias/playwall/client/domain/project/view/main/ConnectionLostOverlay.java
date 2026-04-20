package de.tobias.playwall.client.domain.project.view.main;

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
		background.getStyleClass().add("project-loading--overlay");
		background.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		background.addEventFilter(MouseEvent.ANY, Event::consume);

		final VBox contentBox = new VBox(10);
		contentBox.setAlignment(Pos.CENTER_LEFT);
		contentBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		contentBox.getStyleClass().add("project-loading--box");

		final Label titleLabel = new Label(Localization.getString(Strings.UI_CONNECTION_LOST_TITLE));
		titleLabel.getStyleClass().add("project-loading--label");

		final Label descriptionLabel = new Label(Localization.getString(Strings.UI_CONNECTION_LOST_DESCRIPTION));
		descriptionLabel.getStyleClass().add("project-loading--label");

		final Button closeButton = new Button(Localization.getString(Strings.UI_CONNECTION_LOST_BUTTON_CLOSE));
		closeButton.setOnAction(_ -> onClose.run());

		final HBox buttonBar = new HBox(closeButton);
		buttonBar.setAlignment(Pos.CENTER_RIGHT);
		buttonBar.setPadding(new Insets(6, 0, 0, 0));

		contentBox.getChildren().addAll(titleLabel, descriptionLabel, buttonBar);

		getChildren().addAll(background, contentBox);

		addEventFilter(KeyEvent.ANY, Event::consume);

		setVisible(false);
	}
}
