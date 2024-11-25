package de.tobias.playwall.common.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ProjectNameAlreadyExistsError.class, name = "ProjectNameAlreadyExistsError"),
		@JsonSubTypes.Type(value = ProjectNotExistsError.class, name = "ProjectNotExistsError")
})
public class ServerError
{
}
