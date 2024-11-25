package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.net.ResponseMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ProjectAddResponse extends ResponseMessage
{
	private ProjectMetadataDto project;

	public ProjectAddResponse(UUID messageId, ProjectMetadataDto project)
	{
		super(messageId);
		this.project = project;
	}
}
