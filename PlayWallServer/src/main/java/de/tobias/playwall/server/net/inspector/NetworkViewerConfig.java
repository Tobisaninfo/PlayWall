package de.tobias.playwall.server.net.inspector;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class NetworkViewerConfig implements WebMvcConfigurer
{
	@Bean
	FilterRegistrationBean<LocalhostOnlyFilter> networkViewerLocalhostFilter()
	{
		final FilterRegistrationBean<LocalhostOnlyFilter> registration = new FilterRegistrationBean<>(new LocalhostOnlyFilter());
		registration.addUrlPatterns("/network-viewer", "/network-viewer/*", "/api/network-log", "/api/network-log/*");
		return registration;
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry)
	{
		registry.addViewController("/network-viewer").setViewName("forward:/network-viewer/index.html");
	}
}
