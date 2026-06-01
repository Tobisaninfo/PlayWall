package de.tobias.playwall.client.domain.pad;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.project.ColorMapper;
import de.tobias.playwall.client.domain.project.FadeSettingsMapper;
import de.tobias.playwall.common.api.pad.PadDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class PadMapper
{
	private final PadContentMapper padContentMapper;
	private final ColorMapper colorMapper;
	private final FadeSettingsMapper fadeSettingsMapper;

	public Pad padDtoToPad(PadDto pad)
	{
		return Pad.builder()
				.id(pad.getId())
				.name(pad.getName())
				.position(pad.getPosition())
				.timeMode(pad.getTimeMode())
				.defaultColor(colorMapper.colorToModernColor(pad.getDefaultColor()))
				.playColor(colorMapper.colorToModernColor(pad.getPlayColor()))
				.introColor(colorMapper.colorToModernColor(pad.getIntroColor()))
				.eofWarningTime(pad.getEofWarningTime())
				.introDuration(pad.getIntroDuration())
				.fadeSettings(pad.getFadeSettings() != null ? fadeSettingsMapper.fadeSettingsDtoToFadeSettings(pad.getFadeSettings()) : null)
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
				.introColor(colorMapper.modernColorToColor(pad.getIntroColor()))
				.eofWarningTime(pad.getEofWarningTime())
				.introDuration(pad.getIntroDuration())
				.fadeSettings(pad.getFadeSettings() != null ? fadeSettingsMapper.fadeSettingsToFadeSettingsDto(pad.getFadeSettings()) : null)
				.content(padContentMapper.padContentToPadContentDto(pad.getContent()))
				.build();
	}
}
