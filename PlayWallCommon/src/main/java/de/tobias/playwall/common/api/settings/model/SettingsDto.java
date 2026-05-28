package de.tobias.playwall.common.api.settings.model;

import lombok.Builder;

@Builder
public record SettingsDto(boolean autoLoadLatestProjectOnStart, UnsavedChangesMode unsavedChangesMode, String selectedAudioDevice)
{
}
