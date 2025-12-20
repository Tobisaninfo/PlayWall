package de.tobias.playwall.server.common.model.project;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
public class Pad
{
	private UUID id;
	private Integer position;
	private String name;
	private PadContent content;

	public Pad copy()
	{
		return Pad.builder()
				.id(UUID.randomUUID())
				.position(this.getPosition())
				.name(this.getName())
				.build();
	}
}
