package de.tobias.playwall.server.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

@Component
class MessageSourceConfiguration
{
	@Bean
	public MessageSource messageSource()
	{
		final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:de/tobias/playwall/server/localization/");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}
}
