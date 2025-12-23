package de.tobias.playwall.server.common.model.project;

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
	private double volume;

	@Override
	public PadContent copy()
	{
		return AudioPadContent.builder()
				.loop(this.isLoop())
				.volume(this.getVolume())
				.build();
	}
}
