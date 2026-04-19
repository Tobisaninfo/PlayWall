package de.tobias.playwall.common.api.project.request;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class ProjectRenameRequest extends RequestMessage
{
	private UUID projectId;
	private String newName;
}
