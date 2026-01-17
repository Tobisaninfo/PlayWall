package de.tobias.playwall.client.utils;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.api.project.model.TimeMode;
import javafx.util.Duration;

import java.text.MessageFormat;

@Service
public class PadTimeUtils
{
	private static final String DURATION_FORMAT = "%d:%02d";

	public String formatTimeMode(TimeMode timeMode, Duration duration, Duration position)
	{
		switch(timeMode)
		{
			case ELAPSED ->
			{
				return formatDurationToString(position);
			}
			case REMAINING ->
			{
				return formatDurationToString(duration.subtract(position));
			}
			case ELAPSED_AND_TOTAL ->
			{
				return MessageFormat.format("{0} / {1}", formatDurationToString(position), formatDurationToString(duration));
			}
			default -> throw new IllegalStateException("Unexpected value: " + timeMode);
		}
	}

	public String formatDurationToString(Duration duration)
	{
		int seconds = (int) ((duration.toMillis() / 1000) % 60);
		int minutes = (int) (duration.toMillis() / (1000 * 60));
		return String.format(DURATION_FORMAT, minutes, seconds);
	}
}
