package de.tobias.playwall.common.net;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@class")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
abstract sealed class BaseMessage
		permits RequestMessage, ResponseMessage
{
	private UUID messageId;
}
