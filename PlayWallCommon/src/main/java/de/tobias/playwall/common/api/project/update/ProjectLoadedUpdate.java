package de.tobias.playwall.common.api.project.update;

import de.tobias.playwall.common.api.project.model.ProjectDto;
import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class ProjectLoadedUpdate extends UpdateMessage
{
	private ProjectDto project;
}
