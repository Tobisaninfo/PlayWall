package de.tobias.playwall.server.common.model.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
