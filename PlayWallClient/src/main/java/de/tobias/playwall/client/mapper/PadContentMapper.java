package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.model.project.AudioPadContent;
import de.tobias.playwall.client.model.project.PadContent;
import de.tobias.playwall.common.api.project.model.AudioPadContentDto;
import de.tobias.playwall.common.api.project.model.PadContentDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class PadContentMapper
{
	public PadContent padContentDtoToPadContent(PadContentDto padContent)
	{
		if(padContent == null)
		{
			return null;
		}
		return switch(padContent)
		{
			case AudioPadContentDto audioPadDto -> AudioPadContent.builder()
					.mediaPath(audioPadDto.getMediaPath())
					.isLoop(audioPadDto.isLoop())
					.volume(audioPadDto.getVolume())
					.build();
		};
	}
}
