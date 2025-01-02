package de.tobias.playwall.nativeaudio.audio.windows;


import de.tobias.playwall.server.common.audio.AudioCapability;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.pad.content.PadContent;

public class NativeAudioWinHandlerFactory extends AudioHandlerFactory
{
	public NativeAudioWinHandlerFactory(String type)
	{
		super(type);
	}

	@Override
	public AudioHandler createAudioHandler(PadContent content)
	{
		return new NativeAudioWinHandler(content);
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
