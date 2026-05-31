package de.tobias.playwall.common.api.project.model;

public record FadeSettingsDto(Double fadeInDuration, Boolean fadeInOnPlay, Boolean fadeInOnResume,
                              Double fadeOutDuration, Boolean fadeOutOnPause, Boolean fadeOutOnStop,
                              Boolean fadeOutOnEndOfFile)
{
}
