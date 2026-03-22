package de.tobias.playwall.server.common.model.pad;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.common.TimeMode;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
public class Pad
{
	private UUID id;
	private Integer position;
	private String name;
	private PadContent content;
	private TimeMode timeMode;
	private Color defaultColor;
	private Color playColor;
	private Color introColor;
	private Double eofWarningTime;
	private Double introDuration;

	public Pad copy(boolean generateNewId)
	{
		return Pad.builder()
				.id(generateNewId ? UUID.randomUUID() : id)
				.position(position)
				.name(name)
				.timeMode(timeMode)
				.defaultColor(defaultColor)
				.playColor(playColor)
				.introColor(introColor)
				.eofWarningTime(eofWarningTime)
				.introDuration(introDuration)
				.content(content == null ? null : content.copy())
				.build();
	}
}
