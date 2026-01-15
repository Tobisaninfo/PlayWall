package de.tobias.playwall.client.view.components;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.text.MessageFormat;
import java.util.Locale;

public class VolumeSlider extends HBox
{
	public static final int DEFAULT_VALUE = 100;
	public static final int BOOSTED_VALUE = 115;
	private static final int MIN_VALUE = 0;
	private static final int SNAP_DELTA = 3;

	private final Label label;
	private final Slider slider;

	public VolumeSlider(int maxValue)
	{
		label = new Label();
		label.setStyle("-fx-font-weight: bold");
		label.setMinWidth(35);

		slider = new Slider(MIN_VALUE, Math.min(maxValue, BOOSTED_VALUE), DEFAULT_VALUE);
		slider.setMajorTickUnit(DEFAULT_VALUE);
		slider.setMinorTickCount(0);
		slider.setBlockIncrement(1);
		slider.setShowTickLabels(false);
		slider.setShowTickMarks(false);
		slider.setSnapToTicks(false);
		slider.setPrefWidth(250);

		slider.valueProperty().addListener((_, _, newVal) -> {
			if(Math.abs(newVal.doubleValue() - DEFAULT_VALUE) < SNAP_DELTA)
			{
				slider.setValue(DEFAULT_VALUE);
				label.setText(DEFAULT_VALUE + "%");
			}
			else
			{
				label.setText(newVal.intValue() + "%");
			}
		});

		slider.styleProperty().bind(Bindings.createStringBinding(() -> createSliderStyle(slider.getMin(), slider.getMax(), slider.getValue()), slider.valueProperty()));

		slider.setOnMouseClicked(event -> {
			if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
			{
				slider.setValue(DEFAULT_VALUE);
			}
		});

		getChildren().addAll(slider, label);
		setSpacing(ViewConstants.DEFAULT_SPACING);
		setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(slider, Priority.ALWAYS);
	}

	public double getValue()
	{
		return slider.getValue();
	}

	public void setValue(double value)
	{
		slider.setValue(value);
		label.setText((int) value + "%");
	}

	public DoubleProperty valueProperty()
	{
		return slider.valueProperty();
	}

	private String createSliderStyle(double min, double max, double value)
	{
		final double percentage = 100.0 * (value - min) / (max - min);

		final MessageFormat messageFormat = new MessageFormat(
				"-slider-track-color: linear-gradient(to right, " +
						"{0} 0%, " +
						"{0} {2}%, " +
						"{1} {2}%, " +
						"{1} {3}%, " +
						"{0} {3}%, " +
						"{0} 100%);", Locale.ENGLISH);

		final String color = value <= DEFAULT_VALUE ? ViewConstants.PRIMARY_COLOR : ViewConstants.DANGER_COLOR;

		final Object[] arguments = {ViewConstants.SLIDER_DEFAULT_BACKGROUND_COLOR, color, min, percentage};
		return messageFormat.format(arguments, new StringBuffer(), null).toString();
	}
}
