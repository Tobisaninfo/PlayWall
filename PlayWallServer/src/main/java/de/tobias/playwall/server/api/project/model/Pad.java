package de.tobias.playwall.server.api.project.model;

import de.tobias.playwall.common.api.project.model.PadStatus;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class Pad
{
	private UUID id;
	private Integer position;
	private String name;
	private PadStatus status;
	private List<String> mediaPaths;
}
