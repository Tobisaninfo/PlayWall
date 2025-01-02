package de.tobias.playwall.server.common.audio;


import de.tobias.playwall.server.common.pad.content.PadContent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AudioHandlerFactory
{
	private final String type;

	public abstract AudioHandler createAudioHandler(PadContent content);

	public abstract boolean isFeatureAvailable(AudioCapability audioCapability);
}
