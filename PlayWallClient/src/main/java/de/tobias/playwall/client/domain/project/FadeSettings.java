package de.tobias.playwall.client.domain.project;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class FadeSettings
{
	private Double fadeInDuration = 0.0;
	private Boolean fadeInOnPlay = false;
	private Boolean fadeInOnResume = false;

	private Double fadeOutDuration = 0.0;
	private Boolean fadeOutOnPause = false;
	private Boolean fadeOutOnStop = false;
	private Boolean fadeOutOnEndOfFile = false;
}
