package de.tobias.playwall.client.utils;

import de.tobias.playwall.common.api.common.TimeMode;
import javafx.util.Duration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class PadTimeUtilsTest
{
	static Stream<Arguments> durationArguments()
	{
		return Stream.of(
				of(Duration.seconds(0), "0:00"),
				of(Duration.seconds(13), "0:13"),
				of(Duration.seconds(60), "1:00"),
				of(Duration.seconds(183), "3:03"),
				of(Duration.seconds(610), "10:10"),
				of(Duration.seconds(3605), "60:05"),
				of(Duration.millis(610000), "10:10")
		);
	}

	@ParameterizedTest
	@MethodSource("durationArguments")
	void testFormatDurationToString(Duration durationInput, String expected)
	{
		final String formattedDuration = new PadTimeUtils().formatDurationToString(durationInput);
		assertThat(formattedDuration).isEqualTo(expected);
	}

	static Stream<Arguments> timeModeArguments()
	{
		return Stream.of(
				of(TimeMode.ELAPSED, Duration.seconds(10), Duration.seconds(5), "0:05"),
				of(TimeMode.REMAINING, Duration.seconds(10), Duration.seconds(3), "-0:07"),
				of(TimeMode.ELAPSED_AND_TOTAL, Duration.seconds(10), Duration.seconds(3), "0:03 / 0:10")
		);
	}

	@ParameterizedTest
	@MethodSource("timeModeArguments")
	void testFormatTimeMode(TimeMode timeMode, Duration durationInput, Duration positionInput, String expected)
	{
		final String formattedDuration = new PadTimeUtils().formatTimeMode(timeMode, durationInput, positionInput);
		assertThat(formattedDuration).isEqualTo(expected);
	}
}
