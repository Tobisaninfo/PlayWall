package de.tobias.playwall.client.service;

import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.PadStatus;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientPadController
{
	@Setter(AccessLevel.PACKAGE)
	private Pad pad;

	private PadStatus status;
	private Duration duration;
	private Duration position;

	public ClientPadController(Pad pad)
	{
		this.pad = pad;
	}
}
