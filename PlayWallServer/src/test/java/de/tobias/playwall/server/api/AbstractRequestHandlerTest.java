package de.tobias.playwall.server.api;

import de.tobias.playwall.server.api.settings.SettingsService;
import de.tobias.playwall.server.common.model.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ExtendWith(MockitoExtension.class)
public abstract class AbstractRequestHandlerTest
{
	@MockitoBean
	private SettingsService settingsService;

	@BeforeEach
	void initSettingsService()
	{
		when(settingsService.getSettings()).thenReturn(Settings.DEFAULT);
	}
}
