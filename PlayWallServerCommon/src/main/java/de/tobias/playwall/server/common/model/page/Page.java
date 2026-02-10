package de.tobias.playwall.server.common.model.page;

import de.tobias.playwall.server.common.model.pad.Pad;
import lombok.*;

import java.util.List;
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

	@SuppressWarnings("java:S6204")
	public Page copy()
	{
		return Page.builder()
				.id(UUID.randomUUID())
				.position(position)
				.name(name)
				.pads(pads == null ? null : pads.stream().map(Pad::copy).collect(Collectors.toList()))
				.build();
	}
}
