package de.tobias.playwall.client.domain.project.view.main;

import de.thecodelabs.utils.util.Localization;
import javafx.animation.FadeTransition;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

public class LoadingView extends StackPane
{
	@Getter
	private final ProgressBar progressBar;
	@Getter
	private final Label label;

	private int numberOfLoadedPads;
	private int padCount;

	private FadeTransition fadeIn;
	private FadeTransition fadeOut;

	public LoadingView()
	{
		// dunkler Hintergrund
		final Region background = new Region();
		background.getStyleClass().add("project-loading--overlay");
		background.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

		// Loading Box
		final VBox loadingBox = new VBox(10);
		loadingBox.setAlignment(Pos.CENTER_LEFT);
		loadingBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		loadingBox.getStyleClass().add("project-loading--box");

		progressBar = new ProgressBar();
		progressBar.setStyle("-fx-progress-color: #316DCE");
		progressBar.setPrefWidth(300);
		progressBar.getStyleClass().add("project-loading--progres-bar");
		label = new Label(Localization.getString("ui.project.loading"));
		label.getStyleClass().add("project-loading--label");

		loadingBox.getChildren().addAll(progressBar, label);

		getChildren().addAll(background, loadingBox);

		addEventFilter(MouseEvent.ANY, Event::consume);
		addEventFilter(KeyEvent.ANY, Event::consume);

		fadeIn = new FadeTransition(Duration.millis(200), this);
		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(0.5);

		fadeOut = new FadeTransition(Duration.millis(200), this);
		fadeOut.setFromValue(0.5);
		fadeOut.setToValue(0.0);

		fadeOut.setOnFinished(e -> setVisible(false));
	}

	public void incrementProgress()
	{
		numberOfLoadedPads++;
		progressBar.setProgress(numberOfLoadedPads / (double) padCount);
	}

	public void resetAndSetPadCount(int padCount)
	{
		numberOfLoadedPads = 0;
		progressBar.setProgress(0);

		this.padCount = padCount;
	}

	public void show()
	{
		setOpacity(0);
		setVisible(true);
		fadeIn.playFromStart();
	}

	public void hide()
	{
		fadeOut.playFromStart();
	}
}
