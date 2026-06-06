package de.tobias.playwall.server.api.settings;

import de.tobias.playwall.server.common.model.settings.Settings;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService
{
	private final SettingsRepository settingsRepository;

	@Getter
	private Settings settings;

	@PostConstruct
	void init()
	{
		try
		{
			settings = settingsRepository.loadSettings();
		}
		catch(IOException e)
		{
			log.error("Error parsing settings", e);
		}
	}

	public void updateSettings(Settings settings) throws IOException
	{
		this.settings = settings;
		settingsRepository.saveSettings(settings);
	}
}
