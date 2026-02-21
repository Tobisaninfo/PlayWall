package de.tobias.playwall.server.api.settings.handler;

import de.tobias.playwall.common.api.settings.SettingsGetRequest;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.settings.SettingsRepository;
import de.tobias.playwall.server.common.model.settings.Settings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SettingsGetHandlerTest
{
	@Autowired
	private JsonMapper objectMapper;

	@MockitoBean
	private SettingsRepository settingsRepository;

	@Autowired
	private SettingsGetHandler handler;

	@Test
	void testSettingsGetRequest() throws Exception
	{
		final Settings settings = TestUtils.loadSettings(objectMapper, "settings.json");
		when(settingsRepository.loadSettings()).thenReturn(settings);

		final Optional<ResponseMessage> response = handler.handleRequest(new SettingsGetRequest());

		assertThat(response).isNotEmpty();
	}
}
