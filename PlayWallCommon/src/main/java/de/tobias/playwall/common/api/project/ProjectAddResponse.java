package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class ProjectAddResponse extends ResponseMessage
{
	private ProjectMetadataDto project;

	public ProjectAddResponse(UUID messageId, ProjectMetadataDto project)
	{
		super(messageId);
		this.project = project;
	}
}
