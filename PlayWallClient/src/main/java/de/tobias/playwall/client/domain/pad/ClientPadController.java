package de.tobias.playwall.client.domain.pad;

import de.tobias.playwall.client.domain.project.ProjectMetadata;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientPadController
{
	@Setter
	private Pad pad;

	private PadStatus status;
	private Duration duration;
	private Duration position;
	private ProjectMetadata	projectMetadata;

	public ClientPadController(Pad pad, ProjectMetadata projectMetadata)
	{
		this.pad = pad;
		this.projectMetadata = projectMetadata;
	}
}
