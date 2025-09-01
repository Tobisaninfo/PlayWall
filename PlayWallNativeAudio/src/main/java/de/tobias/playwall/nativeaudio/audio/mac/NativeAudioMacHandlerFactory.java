package de.tobias.playwall.nativeaudio.audio.mac;

import de.tobias.playwall.nativeaudio.audio.mac.delegate.AVAudioPlayerBridgeDelegate;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.common.util.condition.OperatingSystemConditions;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Conditional(OperatingSystemConditions.MacOSCondition.class)
@RequiredArgsConstructor
//@Service
public class NativeAudioMacHandlerFactory implements AudioHandlerFactory
{
	private final AVAudioBridgeHolder bridgeHolder;
	private final AVAudioPlayerBridgeDelegate bridgeDelegate;

	@Override
	public AudioHandler createAudioHandler(PadController padController)
	{
		final NativeAudioMacHandler nativeAudioMacHandler = new NativeAudioMacHandler(padController);
		nativeAudioMacHandler.getBridge().setDelegate(bridgeDelegate);
		bridgeHolder.addHandler(nativeAudioMacHandler);
		return nativeAudioMacHandler;
	}
}
