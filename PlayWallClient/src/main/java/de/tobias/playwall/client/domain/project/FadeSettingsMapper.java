package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.api.project.model.FadeSettingsDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = @InjectConstructor)
public class FadeSettingsMapper
{
	public FadeSettings fadeSettingsDtoToFadeSettings(FadeSettingsDto fadeSettingsDto)
	{
		return new FadeSettings(
				fadeSettingsDto.fadeInDuration(),
				fadeSettingsDto.fadeInOnPlay(),
				fadeSettingsDto.fadeInOnResume(),
				fadeSettingsDto.fadeOutDuration(),
				fadeSettingsDto.fadeOutOnPause(),
				fadeSettingsDto.fadeOutOnStop(),
				fadeSettingsDto.fadeOutOnEndOfFile()
		);
	}

	public FadeSettingsDto fadeSettingsToFadeSettingsDto(FadeSettings fadeSettings)
	{
		return new FadeSettingsDto(
				fadeSettings.getFadeInDuration(),
				fadeSettings.getFadeInOnPlay(),
				fadeSettings.getFadeInOnResume(),
				fadeSettings.getFadeOutDuration(),
				fadeSettings.getFadeOutOnPause(),
				fadeSettings.getFadeOutOnStop(),
				fadeSettings.getFadeOutOnEndOfFile()
		);
	}
}
