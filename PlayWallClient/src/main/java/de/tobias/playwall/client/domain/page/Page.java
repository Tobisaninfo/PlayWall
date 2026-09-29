package de.tobias.playwall.client.domain.page;

import de.tobias.playwall.client.domain.pad.Pad;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Page
{
	private final UUID id;

	private PageSettings settings;

	private Integer position;
	private final List<Pad> pads;

	public Pad getPad(int position)
	{
		return pads.stream().filter(p -> p.getPosition() == position).findFirst().orElse(null);
	}

	public void replacePad(Pad source, Pad target)
	{
		final int index = pads.indexOf(target);
		pads.set(index, source);
	}

	public void removePad(Pad pad)
	{
		pads.remove(pad);
	}

	public void insertPad(Pad pad)
	{
		if(pad.getPosition() > pads.size())
		{
			pads.add(pad);
		}
		else
		{
			pads.add(pad.getPosition(), pad);
		}
	}
}
