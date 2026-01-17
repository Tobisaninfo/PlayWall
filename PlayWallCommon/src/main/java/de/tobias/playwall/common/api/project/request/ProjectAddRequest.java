package de.tobias.playwall.common.api.project.request;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@ToString(callSuper = true)
public class ProjectAddRequest extends RequestMessage
{
	private String name;
	private int numberOfHorizontalPads;
	private int numberOVerticalPads;
}
