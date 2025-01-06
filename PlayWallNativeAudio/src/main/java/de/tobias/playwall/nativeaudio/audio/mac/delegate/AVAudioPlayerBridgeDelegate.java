package de.tobias.playwall.nativeaudio.audio.mac.delegate;

import de.tobias.playwall.common.api.project.model.PadControllerStatus;
import de.tobias.playwall.nativeaudio.audio.mac.AVAudioPlayerBridge;
import de.tobias.playwall.nativeaudio.audio.mac.NativeAudioMacHandler;
import de.tobias.playwall.nativeaudio.audio.mac.NativeAudioMacHandlerFactory;
import de.tobias.playwall.server.common.DurationHelper;
import de.tobias.playwall.server.common.audio.Peakable;
import de.tobias.playwall.server.common.project.PadController;

import java.util.Optional;

public class AVAudioPlayerBridgeDelegate implements AVAudioPlayerBridge.NativeAudioDelegate
{

	private final NativeAudioMacHandlerFactory factory;

	public AVAudioPlayerBridgeDelegate(NativeAudioMacHandlerFactory factory)
	{
		this.factory = factory;
	}

	@Override
	public void onFinish(AVAudioPlayerBridge bridge)
	{
		Optional<NativeAudioMacHandler> nativeAudioMacHandler = factory.getHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(handler -> {
			PadController padController = handler.getController();
			if(padController != null)
			{
				padController.setStatus(PadControllerStatus.EOF);
			}
		});
	}

	@Override
	public void onPositionChanged(AVAudioPlayerBridge bridge, double position)
	{
		Optional<NativeAudioMacHandler> nativeAudioMacHandler = factory.getHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(audioMacHandler -> audioMacHandler.setPosition(DurationHelper.convertSecondsToDuration(position)));
	}

	@Override
	public void onPeakMeter(AVAudioPlayerBridge bridge, float left, float right)
	{
		Optional<NativeAudioMacHandler> nativeAudioMacHandler = factory.getHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(handler -> {
			handler.setAudioLevel(Peakable.Channel.LEFT, left);
			handler.setAudioLevel(Peakable.Channel.RIGHT, right);
		});
	}
}
