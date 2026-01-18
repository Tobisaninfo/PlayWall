package de.tobias.playwall.client.domain.pad;

import de.tobias.playwall.common.api.common.TimeMode;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pad
{
	private UUID id;
	private Integer position;
	private String name;
	private PadContent content;
	private TimeMode timeMode;

	public String getReadablePosition()
	{
		return String.valueOf(position + 1);
	}

	public Pad copy()
	{
		return Pad.builder()
				.id(id)
				.position(position)
				.name(name)
				.timeMode(timeMode)
				.content(content == null ? null : content.copy())
				.build();
	}
}
