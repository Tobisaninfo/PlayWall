package de.tobias.playwall.common.net;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.util.UUID;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@class")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public abstract sealed class BaseMessage
		permits RequestMessage, ResponseMessage, UpdateMessage
{
	private UUID messageId;
}
