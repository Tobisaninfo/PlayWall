package de.tobias.playwall.common.net.project;

import de.tobias.playwall.common.net.ResponseMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ProjectListResponse extends ResponseMessage
{
	private List<ProjectMetadata> projects;

	public ProjectListResponse(UUID messageId, List<ProjectMetadata> projects)
	{
		super(messageId);
		this.projects = projects;
	}
}
