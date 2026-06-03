package de.tobias.playwall.client.domain.project;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class FadeSettings
{
	private Double fadeInDuration = 0.0;
	private Boolean fadeInOnPlay = false;
	private Boolean fadeInOnResume = false;

	private Double fadeOutDuration = 0.0;
	private Boolean fadeOutOnPause = false;
	private Boolean fadeOutOnStop = false;
	private Boolean fadeOutOnEndOfFile = false;

	public FadeSettings copy()
	{
		return FadeSettings.builder()
				.fadeInDuration(this.fadeInDuration)
				.fadeInOnPlay(this.fadeInOnPlay)
				.fadeInOnResume(this.fadeInOnResume)
				.fadeOutDuration(this.fadeOutDuration)
				.fadeOutOnPause(this.fadeOutOnPause)
				.fadeOutOnStop(this.fadeOutOnStop)
				.fadeOutOnEndOfFile(this.fadeOutOnEndOfFile)
				.build();
	}
}
