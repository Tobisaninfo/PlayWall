package de.tobias.playwall.client.domain.project.view.main;

import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class ProjectLoadingView extends StackPane
{
	@Getter
	private final ProgressBar progressBar;
	@Getter
	private final Label label;

	private int loadedPads;
	private int padCount;

	public ProjectLoadingView()
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
		label = new Label("Projekt laden... ");
		label.getStyleClass().add("project-loading--label");

		loadingBox.getChildren().addAll(progressBar, label);

		getChildren().addAll(background, loadingBox);

		addEventFilter(MouseEvent.ANY, Event::consume);
		addEventFilter(KeyEvent.ANY, Event::consume);
	}

	public void incrementProgress()
	{
		loadedPads++;
		progressBar.setProgress(loadedPads / (double) padCount);
	}

	public void resetAndSetPadCount(int padCount)
	{
		loadedPads = 0;
		progressBar.setProgress(0);

		this.padCount = padCount;
	}
}
