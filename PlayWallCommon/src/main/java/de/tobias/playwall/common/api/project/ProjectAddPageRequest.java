package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ProjectAddPageRequest extends RequestMessage
{
	private UUID projectId;
	private String name;

	public ProjectAddPageRequest(UUID projectId, String name)
	{
		this.projectId = projectId;
		this.name = name;
	}
}
