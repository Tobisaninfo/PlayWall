package de.tobias.playwall.common.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ProjectNameAlreadyExistsError.class, name = "ProjectNameAlreadyExistsError")
})
public class ServerError
{
}
