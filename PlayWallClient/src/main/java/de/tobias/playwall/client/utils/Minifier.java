package de.tobias.playwall.client.utils;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Minifier
{
	private static final String[] expressions = {
			"\n",
			"\t"
	};

	public static String minify(String input)
	{
		for(String expression : expressions)
		{
			input = input.replaceAll(expression, "");
		}
		return input;
	}
}
