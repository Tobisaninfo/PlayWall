package de.tobias.playwall.client.appcontext.loader.test.good;

import de.tobias.playwall.client.appcontext.Bean;
import de.tobias.playwall.client.appcontext.Configuration;

@Configuration
public class TestConfiguration
{
	public static class BeanInstance
	{

	}

	@Bean
	public BeanInstance beanInstance()
	{
		return new BeanInstance();
	}
}
