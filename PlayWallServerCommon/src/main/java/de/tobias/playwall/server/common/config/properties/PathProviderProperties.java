package de.tobias.playwall.server.common.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "de.tobias.playwall.path-provider")
@Getter
@Setter
public class PathProviderProperties
{
	private String baseDirectoryTemplate;
}
