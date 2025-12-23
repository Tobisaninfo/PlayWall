package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.common.api.project.model.AudioPadContentDto;
import de.tobias.playwall.common.api.project.model.PadDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class PadMapper
{
	private final PadContentMapper padContentMapper;

	public Pad padDtoToPad(PadDto pad)
	{
		return Pad.builder()
				.id(pad.getId())
				.name(pad.getName())
				.position(pad.getPosition())
				.content(padContentMapper.padContentDtoToPadContent(pad.getContent()))
				.build();
	}
}
