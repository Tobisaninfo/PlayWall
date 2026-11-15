package de.tobias.playwall.client.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateInfo(String version)
{
}
