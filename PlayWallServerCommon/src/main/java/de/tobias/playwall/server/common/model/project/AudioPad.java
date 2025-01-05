package de.tobias.playwall.server.common.model.project;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public final class AudioPad extends Pad
{
	private Boolean isLoop;
	private Double volume;

	@Override
	public Pad copy()
	{
		return AudioPad.builder()
				.id(UUID.randomUUID())
				.name(this.getName())
				.position(this.getPosition())
				.mediaPaths(this.getMediaPaths())
				.isLoop(this.getIsLoop())
				.volume(this.getVolume())
				.build();
	}
}
