package de.tobias.playwall.common.api.project.model;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.common.TimeMode;
import lombok.Builder;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.UUID;

@Builder
public record ProjectMetadataDto(UUID id, String name, Integer numberOfHorizontalPads, Integer numberOfVerticalPads,
                                 Double volume, TimeMode timeMode, Color defaultColor, Color playColor,
                                 Color introColor, Double eofWarningTime, FadeSettingsDto fadeSettings,
								 String midiDevice, Map<UUID, JsonNode> mappings, UUID selectedMapping, Boolean isSoloMode)
{
}
