package de.tobias.playwall.client.model.project;

import java.util.List;
import java.util.UUID;

public record Page(UUID id, String name, Integer position, List<Pad> pads)
{
	public Pad getPad(int position)
	{
		return pads.stream().filter(p -> p.position() == position).findFirst().orElse(null);
	}
}
