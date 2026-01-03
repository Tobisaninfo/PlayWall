package de.tobias.playwall.client.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class MinifierTest
{
	static Stream<Arguments> arguments()
	{
		return Stream.of(
				of(null, ""),
				of("", ""),
				of("""
								.class {
								    color: red;
								    background-color: blue;
								
								
								}
								""",
						".class { color: red; background-color: blue; }"
				),
				of("""
								.class .secondClass {
									color: red;
								}
								""",
						".class .secondClass { color: red; }"
				),
				of("""
								.class:hover label {
								    color: red;
								}
								""",
						".class:hover label { color: red; }"
				),
				of("""
								/* comment */
								.class {
								    color: red;
								}
								""",
						".class { color: red; }"
				),
				of("""
								/* 
								 comment 
								*/
								.class {
								    color: red;
								}
								""",
						".class { color: red; }"
				)
		);
	}

	@ParameterizedTest
	@MethodSource("arguments")
	void testMinifier(String input, String expected)
	{
		final String minifiedCss = Minifier.minifyCss(input);
		assertThat(minifiedCss).isEqualTo(expected);
	}
}
