package de.tobias.playwall.common.api.project.request;

import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class ProjectExportResponse extends ResponseMessage
{
	private String mimetype;
	private String base64;

	public ProjectExportResponse(UUID messageId, String mimetype, String base64)
	{
		super(messageId);
		this.mimetype = mimetype;
		this.base64 = base64;
	}
}
