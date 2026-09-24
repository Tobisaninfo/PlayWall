package de.tobias.playwall.server.common.migration;

import tools.jackson.databind.JsonNode;

public record JsonMigrationResult(JsonNode node, boolean isMigrated)
{
}
