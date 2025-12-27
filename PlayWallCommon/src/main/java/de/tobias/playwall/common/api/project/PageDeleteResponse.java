package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class PageDeleteResponse extends ResponseMessage
{
	public PageDeleteResponse(UUID messageId)
	{
		super(messageId);
	}
}
