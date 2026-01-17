package de.tobias.playwall.server.common.model.pad;

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
	@EqualsAndHashCode.Exclude
	private UUID id;
	private Integer position;
	private String name;
	private PadContent content;

	public Pad copy()
	{
		return Pad.builder()
				.id(UUID.randomUUID())
				.position(position)
				.name(name)
				.content(content == null ? null : content.copy())
				.build();
	}
}
