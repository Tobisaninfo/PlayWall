package de.tobias.playwall.client.domain.pad;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.api.pad.AudioPadContentDto;
import de.tobias.playwall.common.api.pad.PadContentDto;
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

	public PadContentDto padContentToPadContentDto(PadContent padContent)
	{
		if(padContent == null)
		{
			return null;
		}

		return switch(padContent)
		{
			case AudioPadContent audioPad -> AudioPadContentDto.builder()
					.mediaPath(audioPad.getMediaPath())
					.loop(audioPad.isLoop())
					.volume(audioPad.getVolume())
					.build();
			default -> throw new IllegalStateException("Unexpected value: " + padContent);
		};
	}
}
