package de.tobias.playwall.client.view.style;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;

class CssTemplateProcessorTest
{
	static Stream<Arguments> arguments()
	{
		return Stream.of(
				of("", Map.of(), ""),
				// No variables
				of("""
						.play {
							color: red;
						}
						""", Map.of(), """
						.play {
							color: red;
						}
						"""),
				// Variable in property
				of("""
						.play {
							color: ${#color};
						}
						""", Map.of("color", "red"), """
						.play {
							color: red;
						}
						"""),
				// Variable in selector
				of("""
						.play:${#selector} {
							color: red;
						}
						""", Map.of("selector", "hover"), """
						.play:hover {
							color: red;
						}
						""")
		);
	}

	@ParameterizedTest
	@MethodSource("arguments")
	void testProcess(String template, Map<String, String> variables, String expected)
	{
		final String rendered = CssTemplateProcessor.render(template, variables);
		assertThat(rendered).isEqualTo(expected);
	}

	@Test
	void testProcessNullTemplate()
	{
		final Map<String, String> variables = Map.of();
		assertThatThrownBy(() -> CssTemplateProcessor.render(null, variables))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
