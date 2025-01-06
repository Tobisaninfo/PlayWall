package de.tobias.playwall.nativeaudio;

import de.tobias.playwall.nativeaudio.plugin.loader.AudioModuleLoader;
import de.tobias.playwall.nativeaudio.plugin.loader.WindowsAudioImplLoader;
import nativeaudio.NativeAudio;

import java.io.IOException;

public class Test
{
	public static void main(String[] args) throws IOException
	{
		final AudioModuleLoader loader = new WindowsAudioImplLoader();
		loader.preInit();
		loader.init();

		final NativeAudio audioHandler = new NativeAudio();
		audioHandler.load("D:/Documents/Material Robert/Tobias/SFX/5G.mp3");
		audioHandler.play();
	}
}
