package de.tobias.playwall.nativeaudio.loader;

import de.tobias.playwall.nativeaudio.Jni4NetBridgeInitializer;

import java.io.IOException;

public class WindowsAudioImplLoader implements AudioModuleLoader
{
	@Override
	public void preInit() throws IOException
	{
		Jni4NetBridgeInitializer.initialize();
		Jni4NetBridgeInitializer.loadDll(
				getClass().getClassLoader(),
				"win/",
				"j4n",
				"NativeAudio.j4n.dll",
				"NativeAudio.j4n.dll",
				"NativeAudio.dll",
				"NAudio.dll"
		);
	}
}

