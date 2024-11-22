package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.PadStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class PadMetadata
{
	private UUID id;
	private Integer position;
	private String name;
	private PadStatus status;
}
