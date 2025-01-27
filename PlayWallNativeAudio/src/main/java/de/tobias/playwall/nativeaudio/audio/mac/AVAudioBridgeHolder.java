package de.tobias.playwall.nativeaudio.audio.mac;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AVAudioBridgeHolder
{
	private final List<NativeAudioMacHandler> handlers = new ArrayList<>();

	public Optional<NativeAudioMacHandler> getAudioHandlerByBridge(AVAudioPlayerBridge bridge)
	{
		return handlers.stream().filter(handler -> handler.getBridge().equals(bridge)).findFirst();
	}

	void addHandler(NativeAudioMacHandler handler) {
		handlers.add(handler);
	}
}
