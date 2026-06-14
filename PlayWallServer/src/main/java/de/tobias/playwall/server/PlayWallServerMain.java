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
		boolean debugLogLevel = false;
		if(System.getProperty("app.debug") != null)
		{
			debugLogLevel = Boolean.parseBoolean(System.getProperty("app.debug"));
		}

		if(System.getProperty("app.logdir") == null)
		{
			debugLogLevel = true;
			System.setProperty("app.console", String.valueOf(true));
			System.setProperty("app.logdir", "logs");
		}

		if(debugLogLevel)
		{
			System.setProperty("playwall.native-audio.log-level", "DEBUG");
			System.setProperty("logging.level.de.tobias.playwall", "DEBUG");
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
