package de.tobias.playwall.server;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.nativeaudio.NativeAudioModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(NativeAudioModule.class)
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
