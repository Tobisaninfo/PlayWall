package de.tobias.playwall.client.domain.pad;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.project.ColorMapper;
import de.tobias.playwall.common.api.pad.PadDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class PadMapper
{
	private final PadContentMapper padContentMapper;
	private final ColorMapper colorMapper;

	public Pad padDtoToPad(PadDto pad)
	{
		return Pad.builder()
				.id(pad.getId())
				.name(pad.getName())
				.position(pad.getPosition())
				.timeMode(pad.getTimeMode())
				.defaultColor(colorMapper.colorToModernColor(pad.getDefaultColor()))
				.playColor(colorMapper.colorToModernColor(pad.getPlayColor()))
				.eofWarningTime(pad.getEofWarningTime())
				.content(padContentMapper.padContentDtoToPadContent(pad.getContent()))
				.build();
	}

	public PadDto padToPadDto(Pad pad)
	{
		return PadDto.builder()
				.id(pad.getId())
				.name(pad.getName())
				.position(pad.getPosition())
				.timeMode(pad.getTimeMode())
				.defaultColor(colorMapper.modernColorToColor(pad.getDefaultColor()))
				.playColor(colorMapper.modernColorToColor(pad.getPlayColor()))
				.eofWarningTime(pad.getEofWarningTime())
				.content(padContentMapper.padContentToPadContentDto(pad.getContent()))
				.build();
	}
}
