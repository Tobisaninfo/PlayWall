package de.tobias.playwall.client.domain.page;

import de.tobias.playwall.client.domain.pad.Pad;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@ToString
public class Page
{
	private final UUID id;
	private final String name;
	private final Integer position;
	private final List<Pad> pads;

	public Pad getPad(int position)
	{
		return pads.stream().filter(p -> p.getPosition() == position).findFirst().orElse(null);
	}
}
