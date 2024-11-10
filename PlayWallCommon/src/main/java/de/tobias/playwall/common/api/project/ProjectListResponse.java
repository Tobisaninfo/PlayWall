package de.tobias.playwall.common.api.project;

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
	private List<ProjectMetadataDto> projects;

	public ProjectListResponse(UUID messageId, List<ProjectMetadataDto> projects)
	{
		super(messageId);
		this.projects = projects;
	}
}
