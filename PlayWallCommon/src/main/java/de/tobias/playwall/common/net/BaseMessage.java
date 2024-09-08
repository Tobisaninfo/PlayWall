package de.tobias.playwall.common.net;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.tobias.playwall.common.net.project.ProjectListRequest;
import de.tobias.playwall.common.net.project.ProjectListResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ProjectListRequest.class, name = "ProjectListRequest"),
		@JsonSubTypes.Type(value = ProjectListResponse.class, name = "ProjectListResponse"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseMessage
{
	private UUID messageId;
}
