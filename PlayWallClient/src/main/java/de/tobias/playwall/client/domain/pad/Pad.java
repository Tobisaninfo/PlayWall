package de.tobias.playwall.client.domain.pad;

import de.tobias.playwall.client.view.style.color.ModernColor;
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
	private ModernColor defaultColor;
	private ModernColor playColor;
	private Double eofWarningTime;

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
				.defaultColor(defaultColor)
				.playColor(playColor)
				.eofWarningTime(eofWarningTime)
				.content(content == null ? null : content.copy())
				.build();
	}
}
