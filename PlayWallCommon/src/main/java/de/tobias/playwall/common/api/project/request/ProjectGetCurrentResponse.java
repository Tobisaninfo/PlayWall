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
public class ProjectGetCurrentResponse extends ResponseMessage
{
	private ProjectDto project;
	private Integer currentPageIndex;
	private Map<UUID, PadControllerStatus> padStatusById;

	public ProjectGetCurrentResponse(UUID messageId, ProjectDto project, Integer currentPageIndex, Map<UUID, PadControllerStatus> padStatusById)
	{
		super(messageId);
		this.project = project;
		this.currentPageIndex = currentPageIndex;
		this.padStatusById = padStatusById;
	}
}
