package de.tobias.playwall.client.model.project;

import de.tobias.playwall.common.api.project.model.PadStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
public abstract sealed class Pad permits AudioPad
{
	private UUID id;
	private Integer position;
	private String name;
	private PadStatus status;
	private List<String> mediaPaths;
}
