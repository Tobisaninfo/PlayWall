package de.tobias.playwall.client.utils;

import javafx.util.Duration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class PadTimeUtilsTest
{
	static Stream<Arguments> arguments()
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
	@MethodSource("arguments")
	void testFormatDurationToString(Duration durationInput, String expected)
	{
		final String formattedDuration = new PadTimeUtils().formatDurationToString(durationInput);
		assertThat(formattedDuration).isEqualTo(expected);
	}
}
