package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.model.project.AudioPad;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.common.api.project.model.AudioPadDto;
import de.tobias.playwall.common.api.project.model.PadDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor(access = AccessLevel.PACKAGE)
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
