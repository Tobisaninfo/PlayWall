package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.di.Component;
import de.tobias.playwall.client.model.project.AudioPad;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.common.api.project.model.AudioPadDto;
import de.tobias.playwall.common.api.project.model.PadDto;

@Component
public class PadMapper
{
	public Pad padDtoToPad(PadDto pad)
	{
		return switch(pad)
		{
			case AudioPadDto audioPadDto -> AudioPad.builder()
					.id(audioPadDto.getId())
					.name(audioPadDto.getName())
					.position(audioPadDto.getPosition())
					.mediaPaths(audioPadDto.getMediaPaths())
					.isLoop(audioPadDto.getIsLoop())
					.volume(audioPadDto.getVolume())
					.build();
		};
	}
}
