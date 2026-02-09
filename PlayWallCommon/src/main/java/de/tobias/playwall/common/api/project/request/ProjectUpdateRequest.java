package de.tobias.playwall.common.api.project.request;

import de.tobias.playwall.common.api.project.model.ProjectDto;
import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class ProjectUpdateRequest extends RequestMessage
{
	private ProjectDto project;
}
