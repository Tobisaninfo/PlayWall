package de.tobias.playwall.common.api.pad;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public final class AudioPadContentDto extends PadContentDto
{
	private String mediaPath;

	private boolean loop;
	private double volume;
}
