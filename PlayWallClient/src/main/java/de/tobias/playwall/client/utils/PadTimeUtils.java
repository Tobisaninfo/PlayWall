package de.tobias.playwall.client.utils;

import de.tobias.playwall.client.appcontext.Service;
import javafx.util.Duration;

@Service
public class PadTimeUtils
{
	private static final String DURATION_FORMAT = "%d:%02d";

	public String getTimeString(Duration duration)
	{
		return durationToString(duration);
	}

	private String durationToString(Duration value)
	{
		int seconds = (int) ((value.toMillis() / 1000) % 60);
		int minutes = (int) ((value.toMillis() / (1000 * 60)) % 60);
		return String.format(DURATION_FORMAT, minutes, seconds);
	}
}
