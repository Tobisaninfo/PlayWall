package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.PadStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class Pad
{
	private UUID id;
	private Integer position;
	private String name;
	private PadStatus status;
}
