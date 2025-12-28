package de.tobias.playwall.common.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@class")
public abstract class ServerError implements Serializable
{
	private static final String PATTERN_CAMEL_CASE = "(?<!^)(?=[A-Z])";

	@JsonIgnore
	public String getLocalizationKey()
	{
		final String className = this.getClass().getSimpleName();
		return Arrays.stream(className.split(PATTERN_CAMEL_CASE))
				.map(String::toLowerCase)
				.collect(Collectors.joining("."));
	}

	public abstract Object[] getMessageArguments();
}
