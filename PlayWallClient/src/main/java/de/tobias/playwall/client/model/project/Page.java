package de.tobias.playwall.client.model.project;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
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

	@Override
	public String toString()
	{
		return "Page[" +
			   "id=" + id + ", " +
			   "name=" + name + ", " +
			   "position=" + position + ", " +
			   "pads=" + pads + ']';
	}

}
