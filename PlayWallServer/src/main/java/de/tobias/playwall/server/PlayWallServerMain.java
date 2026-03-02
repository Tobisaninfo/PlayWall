package de.tobias.playwall.server;

import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.nativeaudio.NativeAudioModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(NativeAudioModule.class)
@SpringBootApplication
public class PlayWallServerMain
{
	public static void main(String[] args)
	{
		if(System.getProperty("app.logdir") == null)
		{
			System.setProperty("app.debug", String.valueOf(true));
			System.setProperty("app.logdir", "logs");
		}

		final Logger log = LoggerFactory.getLogger(PlayWallServerMain.class);
		if(OS.isWindows() || OS.isMacOS())
		{
			System.setProperty("java.awt.headless", "false");
			log.debug("Set system property 'java.awt.headless' to true");
		}

		SpringApplication.run(PlayWallServerMain.class, args);
	}
}
