package de.tobias.playwall.server.common.audio;


import de.tobias.playwall.server.common.project.PadController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AudioHandlerFactory
{
	private final String type;

	public abstract AudioHandler createAudioHandler(PadController padController);

	public abstract boolean isFeatureAvailable(AudioCapability audioCapability);
}
