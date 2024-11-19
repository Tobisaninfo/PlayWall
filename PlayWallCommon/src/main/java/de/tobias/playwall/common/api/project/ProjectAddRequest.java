package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ProjectAddRequest extends RequestMessage
{
	private String name;

	public ProjectAddRequest(String name)
	{
		this.name = name;
	}
}
