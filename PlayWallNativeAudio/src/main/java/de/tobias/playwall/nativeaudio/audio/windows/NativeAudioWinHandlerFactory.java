package de.tobias.playwall.nativeaudio.audio.windows;


import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.project.PadController;

public class NativeAudioWinHandlerFactory
{
	public AudioHandler createAudioHandler(PadController padController)
	{
		return new NativeAudioWinHandler(padController);
	}
}
