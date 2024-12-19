package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.project.model.ProjectDto;
import de.tobias.playwall.common.net.ResponseMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ProjectLaunchResponse extends ResponseMessage
{
	private ProjectDto project;

	public ProjectLaunchResponse(UUID messageId, ProjectDto project)
	{
		super(messageId);
		this.project = project;
	}
}
