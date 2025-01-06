package de.tobias.playwall.nativeaudio.audio.windows;


import de.tobias.playwall.server.common.audio.AudioCapability;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.project.PadController;

public class NativeAudioWinHandlerFactory extends AudioHandlerFactory
{
	public NativeAudioWinHandlerFactory(String type)
	{
		super(type);
	}

	@Override
	public AudioHandler createAudioHandler(PadController padController)
	{
		return new NativeAudioWinHandler(padController);
	}

	@Override
	public boolean isFeatureAvailable(AudioCapability audioCapability)
	{
		for(Class<?> clazz : NativeAudioWinHandler.class.getInterfaces())
		{
			if(clazz.equals(audioCapability.getAudioFeature()))
				return true;
		}
		return false;
	}
}
