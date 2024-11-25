package de.tobias.playwall.common.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;

import java.util.Arrays;
import java.util.stream.Collectors;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ProjectNameAlreadyExistsError.class, name = "ProjectNameAlreadyExistsError"),
		@JsonSubTypes.Type(value = ProjectNotExistsError.class, name = "ProjectNotExistsError")
})
public class ServerError
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
}
