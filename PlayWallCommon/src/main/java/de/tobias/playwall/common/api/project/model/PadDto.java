package de.tobias.playwall.common.api.project.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
public abstract sealed class PadDto permits AudioPadDto
{
	private UUID id;
	private Integer position;
	private String name;
	private PadStatus status;
	private List<String> mediaPaths;
}

