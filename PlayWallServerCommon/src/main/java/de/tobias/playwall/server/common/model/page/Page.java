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
	@EqualsAndHashCode.Exclude
	private UUID id;
	private Integer position;
	private String name;
	private List<Pad> pads;

	public Pad getPad(int position)
	{
		return pads.stream().filter(p -> p.getPosition() == position).findFirst().orElse(null);
	}

	public Optional<Pad> getPad(UUID padId)
	{
		return pads.stream().filter(p -> p.getId().equals(padId)).findFirst();
	}

	@SuppressWarnings("java:S6204")
	public Page copy(boolean generateNewId)
	{
		return Page.builder()
				.id(generateNewId ? UUID.randomUUID() : id)
				.position(position)
				.name(name)
				.pads(pads == null ? null : pads.stream().map(pad -> pad.copy(generateNewId)).collect(Collectors.toList()))
				.build();
	}
}
