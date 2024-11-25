package de.tobias.playwall.server;

import de.thecodelabs.utils.util.Localization;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlayWallServerMain
{
	public static void main(String[] args)
	{
		Localization.setDelegate(new PlayWallLocalizationDelegate());
		Localization.load();

		SpringApplication.run(PlayWallServerMain.class, args);
	}
}
