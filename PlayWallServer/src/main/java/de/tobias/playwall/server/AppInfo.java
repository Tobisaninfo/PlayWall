package de.tobias.playwall.server;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppInfo
{
	private final BuildProperties buildProperties;

	public String getVersion()
	{
		return buildProperties.getVersion();
	}
}
