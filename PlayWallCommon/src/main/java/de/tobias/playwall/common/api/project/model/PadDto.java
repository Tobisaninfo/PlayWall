package de.tobias.playwall.common.api.project.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
public class PadDto
{
	private UUID id;
	private Integer position;
	private String name;
	private PadContentDto content;
}

