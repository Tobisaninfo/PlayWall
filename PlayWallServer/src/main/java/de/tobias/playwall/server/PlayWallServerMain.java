package de.tobias.playwall.server;

import de.thecodelabs.utils.util.OS;
import de.tobias.playwall.nativeaudio.NativeAudioModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(NativeAudioModule.class)
@SpringBootApplication
@Slf4j
public class PlayWallServerMain
{
	public static void main(String[] args)
	{
		if(OS.isWindows() || OS.isMacOS())
		{
			System.setProperty("java.awt.headless", "false");
			log.debug("Set system property 'java.awt.headless' to true");
		}

		SpringApplication.run(PlayWallServerMain.class, args);
	}
}
