package de.tobias.playwall.client.view.validation;

@FunctionalInterface
public interface Validator
{
	/**
	 * Validates the given input.
	 *
	 * @return human-readable error message if input is invalid, {@code null} if valid
	 */
	String validate(String input);

	default Validator and(Validator other)
	{
		return input -> {
			String error = this.validate(input);
			return error != null ? error : other.validate(input);
		};
	}
}
