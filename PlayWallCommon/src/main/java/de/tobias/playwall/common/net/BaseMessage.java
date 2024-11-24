package de.tobias.playwall.common.net;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.tobias.playwall.common.api.project.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "$type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ProjectListRequest.class, name = "ProjectListRequest"),
		@JsonSubTypes.Type(value = ProjectListResponse.class, name = "ProjectListResponse"),
		@JsonSubTypes.Type(value = ProjectDeleteRequest.class, name = "ProjectDeleteRequest"),
		@JsonSubTypes.Type(value = ProjectDeleteResponse.class, name = "ProjectDeleteResponse"),
		@JsonSubTypes.Type(value = ProjectAddRequest.class, name = "ProjectAddRequest"),
		@JsonSubTypes.Type(value = ProjectAddResponse.class, name = "ProjectAddResponse"),
		@JsonSubTypes.Type(value = ProjectAddPageRequest.class, name = "ProjectAddPageRequest"),
		@JsonSubTypes.Type(value = ProjectAddPageResponse.class, name = "ProjectAddPageResponse"),
		@JsonSubTypes.Type(value = ErrorMessage.class, name = "ErrorMessage"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
abstract sealed class BaseMessage
		permits RequestMessage, ResponseMessage
{
	private UUID messageId;
}
