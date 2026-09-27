package de.tobias.playwall.common.api.project.request;

import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.project.model.ProjectDto;
import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.Map;
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

	/**
	 * The current playback status of every pad that has content, keyed by pad id. Only populated
	 * together with {@link #currentPageIndex}, for the same reason: a fresh (or reconnecting) client
	 * would otherwise only learn a pad's status from the next {@code PadStatusUpdate} broadcast, which
	 * may never come if nothing changes after the client connects.
	 */
	private Map<UUID, PadControllerStatus> padStatusById;

	public ProjectGetResponse(UUID messageId, ProjectDto project)
	{
		super(messageId);
		this.project = project;
	}

	public ProjectGetResponse(UUID messageId, ProjectDto project, Integer currentPageIndex, Map<UUID, PadControllerStatus> padStatusById)
	{
		super(messageId);
		this.project = project;
		this.currentPageIndex = currentPageIndex;
		this.padStatusById = padStatusById;
	}
}
