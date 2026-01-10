package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class ProjectSettingsUpdateRequest extends RequestMessage
{
	private ProjectMetadataDto projectMetadata;
}
