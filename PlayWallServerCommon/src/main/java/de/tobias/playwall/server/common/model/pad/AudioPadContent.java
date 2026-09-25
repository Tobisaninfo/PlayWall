package de.tobias.playwall.server.common.model.pad;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Builder
@NoArgsConstructor
public final class AudioPadContent extends PadContent
{
	private String mediaPath;

	private boolean loop;

	@Builder.Default
	private double volume = 1.0;

	@Builder.Default
	private double speed = 1.0;

	@Builder.Default
	private boolean ignoreSoloMode = false;

	@Override
	public PadContent copy()
	{
		return AudioPadContent.builder()
				.mediaPath(this.getMediaPath())
				.loop(this.isLoop())
				.volume(this.getVolume())
				.speed(this.getSpeed())
				.ignoreSoloMode(this.isIgnoreSoloMode())
				.build();
	}
}
