package de.tobias.playwall.common.api.project.update;

import de.tobias.playwall.common.net.UpdateMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class ProjectShowPageCommand extends UpdateMessage
{
	private int index;
}
