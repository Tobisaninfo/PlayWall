package de.tobias.playwall.client.model.project;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public final class AudioPad extends Pad
{
	private Boolean isLoop;
	private Double volume;
}
