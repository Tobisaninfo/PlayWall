package de.tobias.playwall.client.view.components.drag;

import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.view.components.PseudoClasses;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import lombok.Getter;

import java.util.Collection;

public class FileDragOptionView
{

	private final HBox optionPane;
	private final Pane parent;

	private final Transition inTransition;
	private final Transition outTransition;

	public FileDragOptionView(Pane pane)
	{
		parent = pane;

		optionPane = new HBox();
		optionPane.getStyleClass().add("dnd-hud");
		optionPane.prefWidthProperty().bind(parent.widthProperty());
		optionPane.prefHeightProperty().bind(parent.heightProperty());
		optionPane.setAlignment(Pos.CENTER);
		optionPane.setPadding(new Insets(5));
		optionPane.setSpacing(5);

		inTransition = createTransition(true);
		outTransition = createTransition(false);

	}

	private Transition createTransition(boolean in)
	{
		final FadeTransition fadeTransition = new FadeTransition();
		fadeTransition.setNode(optionPane);

		final ScaleTransition scaleTransition = new ScaleTransition();
		scaleTransition.setNode(optionPane);

		if(in)
		{
			fadeTransition.setFromValue(0);
			fadeTransition.setToValue(1);

			scaleTransition.setFromX(1.3);
			scaleTransition.setFromY(1.3);
			scaleTransition.setToX(1);
			scaleTransition.setToY(1);
		}
		else
		{
			fadeTransition.setFromValue(1);
			fadeTransition.setToValue(0);

			scaleTransition.setFromX(1);
			scaleTransition.setFromY(1);
			scaleTransition.setToX(1.3);
			scaleTransition.setToY(1.3);
		}

		final ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, scaleTransition);
		parallelTransition.setOnFinished(_ ->
		{
			if(!in)
				parent.getChildren().remove(optionPane);
		});
		return parallelTransition;
	}

	@Getter
	private FileDragOption selectedOption;

	public void showOptions(Collection<? extends FileDragOption> options)
	{
		if(!parent.getChildren().contains(optionPane))
		{
			selectedOption = null;

			parent.getChildren().add(optionPane);
			optionPane.getChildren().clear();

			options.stream().sorted().forEach(option -> {
				final Label label = new Label();
				label.getStyleClass().add("dnd-file-option");
				label.setText(option.getLabel());

				final FontIcon graphics = new FontIcon(option.getIcon());
				graphics.setStyle("-fx-text-fill: white;");
				label.setGraphic(graphics);
				label.setWrapText(true);

				label.setOnDragOver(_ ->
				{
					label.pseudoClassStateChanged(PseudoClasses.DRAG_CLASS, true);
					selectedOption = option;
				});
				label.setOnDragExited(_ ->
				{
					label.pseudoClassStateChanged(PseudoClasses.DRAG_CLASS, false);
					selectedOption = null;
				});

				label.setAlignment(Pos.CENTER);
				label.setTextAlignment(TextAlignment.CENTER);
				label.setContentDisplay(ContentDisplay.TOP);

				label.maxWidthProperty().bind(optionPane.widthProperty().divide(options.size()).subtract(12.5));
				label.setMaxHeight(Double.MAX_VALUE);
				HBox.setHgrow(label, Priority.ALWAYS);

				optionPane.getChildren().add(label);
			});

			inTransition.play();
		}
	}

	public void hide()
	{
		outTransition.play();
	}
}
