package de.tobias.playwall.common.api.project.request;

import de.tobias.playwall.common.api.project.model.ProjectDto;
import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class ProjectGetResponse extends ResponseMessage
{
	private ProjectDto project;

	/**
	 * The index of the page currently shown in the PlayWall client. Only populated when this
	 * response was for the currently loaded project (i.e. the request's projectId was null);
	 * {@code null} for an explicit by-id lookup.
	 */
	private Integer currentPageIndex;

	public ProjectGetResponse(UUID messageId, ProjectDto project)
	{
		super(messageId);
		this.project = project;
	}

	public ProjectGetResponse(UUID messageId, ProjectDto project, Integer currentPageIndex)
	{
		super(messageId);
		this.project = project;
		this.currentPageIndex = currentPageIndex;
	}
}
