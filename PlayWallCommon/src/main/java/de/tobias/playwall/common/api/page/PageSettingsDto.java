package de.tobias.playwall.common.api.page;

import de.tobias.playwall.common.api.common.Color;
import lombok.Builder;

@Builder
public record PageSettingsDto(String name, Color color)
{
}
