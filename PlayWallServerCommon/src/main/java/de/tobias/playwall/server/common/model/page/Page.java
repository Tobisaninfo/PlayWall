package de.tobias.playwall.server.common.model.page;

import de.tobias.playwall.server.common.model.pad.Pad;
import lombok.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@EqualsAndHashCode
public class Page
{
	private UUID id;

	private PageSettings settings;

	private Integer position;
	private List<Pad> pads;

	public Pad getPad(int position)
	{
		return pads.stream().filter(p -> p.getPosition() == position).findFirst().orElse(null);
	}

	public Optional<Pad> getPad(UUID padId)
	{
		return pads.stream().filter(p -> p.getId().equals(padId)).findFirst();
	}

	public void replacePad(Pad source, Pad target)
	{
		final int index = pads.indexOf(target);
		source.setPosition(target.getPosition());
		pads.set(index, source);
	}

	@SuppressWarnings("java:S6204")
	public Page copy(boolean generateNewId)
	{
		return Page.builder()
				.id(generateNewId ? UUID.randomUUID() : id)
				.position(position)
				.settings(settings.copy())
				.pads(pads == null ? null : pads.stream().map(pad -> pad.copy(generateNewId)).collect(Collectors.toList()))
				.build();
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
