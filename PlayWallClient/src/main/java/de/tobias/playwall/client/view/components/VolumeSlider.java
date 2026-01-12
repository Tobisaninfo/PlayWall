package de.tobias.playwall.client.view.components;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Slider;

import java.text.MessageFormat;
import java.util.Locale;

public class VolumeSlider extends Slider
{
	private static final int MIN_VALUE = 0;
	private static final int MAX_VALUE = 115;
	private static final int DEFAULT_VALUE = 100;
	private static final int SNAP_DELTA = 3;

	public VolumeSlider()
	{
		setMin(MIN_VALUE);
		setMax(MAX_VALUE);
		setValue(DEFAULT_VALUE);
		setMajorTickUnit(DEFAULT_VALUE);
		setMinorTickCount(0);
		setBlockIncrement(1);
		setShowTickLabels(true);
		setShowTickMarks(true);
		setSnapToTicks(false);
		setPrefWidth(250);

		valueProperty().addListener((_, _, newVal) -> {
			if(Math.abs(newVal.doubleValue() - DEFAULT_VALUE) < SNAP_DELTA)
			{
				setValue(DEFAULT_VALUE);
			}
		});

		styleProperty().bind(Bindings.createStringBinding(() -> createSliderStyle(getMin(), getMax(), getValue()), valueProperty()));
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

		final Object[] arguments = {ViewConstants.SLIDER_DEFAULT_BACKGROUND_COLOR, ViewConstants.PRIMARY_COLOR, min, percentage};
		return messageFormat.format(arguments, new StringBuffer(), null).toString();
	}
}
