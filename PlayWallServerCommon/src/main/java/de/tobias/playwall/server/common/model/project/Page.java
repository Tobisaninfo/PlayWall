package de.tobias.playwall.server.common.model.project;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class Page
{
	private UUID id;
	private Integer position;
	private String name;
	private List<Pad> pads;

	public Pad getPad(int position)
	{
		return pads.stream().filter(p -> p.getPosition() == position).findFirst().orElse(null);
	}
}
