package de.tobias.playwall.server.common;

import lombok.NoArgsConstructor;

import java.time.Duration;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DurationHelper
{
	public static Duration convertSecondsToDuration(double seconds)
	{
		long wholeSeconds = (long) seconds;
		long nanos = (long) ((seconds - wholeSeconds) * 1_000_000_000);
		return Duration.ofSeconds(wholeSeconds, nanos);
	}

	public static Duration convertMillisToDuration(double millis)
	{
		long wholeSeconds = (long) (millis / 1000);
		long nanos = (long) ((millis % 1000) * 1_000_000);
		return Duration.ofSeconds(wholeSeconds, nanos);
	}
}
