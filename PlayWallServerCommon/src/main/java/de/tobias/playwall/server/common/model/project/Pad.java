package de.tobias.playwall.server.common.model.project;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.tobias.playwall.common.api.project.model.PadStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@class")
public abstract sealed class Pad permits AudioPad
{
	private UUID id;
	private Integer position;
	private String name;
	private PadStatus status;
	private List<String> mediaPaths;

	public abstract Pad copy();
}
