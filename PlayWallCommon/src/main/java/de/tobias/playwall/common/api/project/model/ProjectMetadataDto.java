package de.tobias.playwall.common.api.project.model;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.common.TimeMode;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProjectMetadataDto(UUID id, String name, int numberOfHorizontalPads, int numberOfVerticalPads, double volume, TimeMode timeMode, Color defaultColor, Color playColor)
{
}
