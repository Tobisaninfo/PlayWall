package de.tobias.playwall.common.api.project.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public final class AudioPadDto extends PadDto
{
	private Boolean isLoop;
	private Double volume;
}
