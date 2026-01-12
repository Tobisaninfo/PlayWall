package de.tobias.playwall.client.view.components;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Slider;

import java.text.MessageFormat;
import java.util.Locale;

public class VolumeSlider extends Slider
{
	public VolumeSlider()
	{
		setMin(0);
		setMax(115);
		setValue(100);
		setShowTickLabels(true);
		setShowTickMarks(true);
		setSnapToTicks(true);
		setPrefWidth(250);

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
