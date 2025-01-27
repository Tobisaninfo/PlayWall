package de.tobias.playwall.nativeaudio.audio.mac.delegate;

import de.tobias.playwall.common.api.project.model.PadControllerStatus;
import de.tobias.playwall.nativeaudio.audio.mac.AVAudioBridgeHolder;
import de.tobias.playwall.nativeaudio.audio.mac.AVAudioPlayerBridge;
import de.tobias.playwall.nativeaudio.audio.mac.NativeAudioMacHandler;
import de.tobias.playwall.server.common.DurationHelper;
import de.tobias.playwall.server.common.audio.Peakable;
import de.tobias.playwall.server.common.project.PadController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class AVAudioPlayerBridgeDelegate implements NativeAudioDelegate
{
	private final AVAudioBridgeHolder holder;

	@Override
	public void onFinish(AVAudioPlayerBridge bridge)
	{
		final Optional<NativeAudioMacHandler> nativeAudioMacHandler = holder.getAudioHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(handler -> {
			final PadController padController = handler.getController();
			if(padController != null)
			{
				padController.setStatus(PadControllerStatus.EOF);
			}
		});
	}

	@Override
	public void onPositionChanged(AVAudioPlayerBridge bridge, double position)
	{
		final Optional<NativeAudioMacHandler> nativeAudioMacHandler = holder.getAudioHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(audioMacHandler -> audioMacHandler.setPosition(DurationHelper.convertSecondsToDuration(position)));
	}

	@Override
	public void onPeakMeter(AVAudioPlayerBridge bridge, float left, float right)
	{
		final Optional<NativeAudioMacHandler> nativeAudioMacHandler = holder.getAudioHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(handler -> {
			handler.setAudioLevel(Peakable.Channel.LEFT, left);
			handler.setAudioLevel(Peakable.Channel.RIGHT, right);
		});
	}
}
