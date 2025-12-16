package de.tobias.playwall.client.server;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServerLauncherProperties
{
	private String storagePath;
}
