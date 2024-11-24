package de.tobias.playwall.common.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.tobias.playwall.common.api.project.ProjectNameAlreadyExists;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ProjectNameAlreadyExists.class, name = "ProjectNameAlreadyExists")
})
public class ServerError
{
}
