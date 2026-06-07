package de.tobias.playwall.client.domain.pad;

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
public final class AudioPadContent extends PadContent implements Loopable
{
	private String mediaPath;

	private boolean isLoop;
	private double volume;
	private double speed;

	@Override
	public AudioPadContent copy()
	{
		return AudioPadContent.builder()
				.mediaPath(mediaPath)
				.isLoop(isLoop)
				.volume(volume)
				.speed(speed)
				.build();
	}
}
