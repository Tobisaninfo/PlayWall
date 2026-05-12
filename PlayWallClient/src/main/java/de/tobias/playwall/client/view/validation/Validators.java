package de.tobias.playwall.client.view.validation;

import java.util.regex.Pattern;

public final class Validators
{
	private Validators()
	{
	}

	public static Validator notEmpty(String message)
	{
		return input -> (input == null || input.isBlank()) ? message : null;
	}

	public static Validator maxLength(int max, String message)
	{
		return input -> input != null && input.length() > max ? message : null;
	}

	public static Validator pattern(Pattern pattern, String message)
	{
		return input -> input != null && !pattern.matcher(input).matches() ? message : null;
	}
}
