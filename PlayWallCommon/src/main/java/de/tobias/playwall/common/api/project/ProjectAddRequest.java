package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
public class ProjectAddRequest extends RequestMessage
{
	private String name;
}
