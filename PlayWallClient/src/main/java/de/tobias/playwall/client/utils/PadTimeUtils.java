package de.tobias.playwall.client.utils;

import de.tobias.playwall.client.appcontext.Service;
import javafx.util.Duration;

@Service
public class PadTimeUtils
{
	private static final String DURATION_FORMAT = "%d:%02d";

	public String formatDurationToString(Duration duration)
	{
		int seconds = (int) ((duration.toMillis() / 1000) % 60);
		int minutes = (int) (duration.toMillis() / (1000 * 60));
		return String.format(DURATION_FORMAT, minutes, seconds);
	}
}
