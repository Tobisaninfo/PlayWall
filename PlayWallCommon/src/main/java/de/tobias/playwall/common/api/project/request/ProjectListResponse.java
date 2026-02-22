package de.tobias.playwall.common.api.project.request;

import de.tobias.playwall.common.api.project.model.AllProjectsInfoDto;
import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class ProjectListResponse extends ResponseMessage
{
	private AllProjectsInfoDto allProjectsInfo;

	public ProjectListResponse(UUID messageId, AllProjectsInfoDto allProjectsInfo)
	{
		super(messageId);
		this.allProjectsInfo = allProjectsInfo;
	}
}
