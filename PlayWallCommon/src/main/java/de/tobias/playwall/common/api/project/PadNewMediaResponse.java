package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class PadNewMediaResponse extends ResponseMessage
{
	private UUID padId;

	private String newName;

	public PadNewMediaResponse(UUID messageId, UUID padId, String newName)
	{
		super(messageId);
		this.padId = padId;
		this.newName = newName;
	}
}
