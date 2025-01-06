package de.tobias.playwall.nativeaudio.audio.mac;

import de.tobias.playwall.nativeaudio.audio.mac.delegate.AVAudioPlayerBridgeDelegate;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.project.PadController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NativeAudioMacHandlerFactory
{
	private final List<NativeAudioMacHandler> handlers = new ArrayList<>();
	private final AVAudioPlayerBridgeDelegate bridgeDelegate = new AVAudioPlayerBridgeDelegate(this);

	public Optional<NativeAudioMacHandler> getHandlerByBridge(AVAudioPlayerBridge bridge)
	{
		return handlers.stream().filter(handler -> handler.getBridge().equals(bridge)).findFirst();
	}

	public AudioHandler createAudioHandler(PadController padController)
	{
		NativeAudioMacHandler nativeAudioMacHandler = new NativeAudioMacHandler(padController);
		nativeAudioMacHandler.getBridge().setDelegate(bridgeDelegate);
		handlers.add(nativeAudioMacHandler);
		return nativeAudioMacHandler;
	}
}
