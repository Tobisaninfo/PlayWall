package de.tobias.playwall.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.TaskScheduler;

@Configuration
@EnableScheduling
@Profile("!test")
class SchedulingConfig
{
	@Bean
	public TaskScheduler taskScheduler()
	{
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setThreadNamePrefix("scheduling-");
		scheduler.initialize();
		return scheduler;
	}
}
