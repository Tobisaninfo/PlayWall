package de.tobias.playwall.nativeaudio.audio.rust;


import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.common.util.condition.OperatingSystemConditions;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Conditional(OperatingSystemConditions.MacOSCondition.class)
@Service
public class NativeAudioWinHandlerFactory implements AudioHandlerFactory
{
	@Override
	public AudioHandler createAudioHandler(PadController padController)
	{
		return new NativeAudioRustHandler(padController);
	}
}
