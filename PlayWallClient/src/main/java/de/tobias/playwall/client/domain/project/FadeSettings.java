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
	@Builder.Default
	private Double fadeInDuration = 0.0;
	@Builder.Default
	private Boolean fadeInOnPlay = false;
	@Builder.Default
	private Boolean fadeInOnResume = false;

	@Builder.Default
	private Double fadeOutDuration = 0.0;
	@Builder.Default
	private Boolean fadeOutOnPause = false;
	@Builder.Default
	private Boolean fadeOutOnStop = false;
	@Builder.Default
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
