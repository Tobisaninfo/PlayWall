package de.tobias.playwall.server.api.settings.handler;

import de.tobias.playwall.common.api.history.UndoRequest;
import de.tobias.playwall.common.api.settings.SettingsUpdateRequest;
import de.tobias.playwall.common.api.settings.model.SettingsDto;
import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import de.tobias.playwall.common.api.settings.update.SettingsUpdate;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.common.storage.PathProvider;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestExecutor;
import de.tobias.playwall.server.net.RequestHandlerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class SettingsUpdateHandlerTest extends AbstractRequestHandlerTest
{
	@TempDir
	private Path tempDir;

	@Autowired
	protected JsonMapper objectMapper;

	@Autowired
	private RequestExecutor requestExecutor;

	@Autowired
	protected RequestHandlerFactory requestHandlerFactory;

	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private SettingsUpdateHandler handler;

	@MockitoBean
	private PathProvider pathProvider;

	@BeforeEach
	void beforeEach() throws IOException
	{
		final Path projectsFile = tempDir.resolve("settings.json");

		Files.writeString(projectsFile, """
				{
					"VERSION": 1,
					"autoLoadLatestProjectOnStart": false,
					"unsavedChangesMode": "ASK"
				}
				""");

		when(pathProvider.getPathForConfig(any())).thenReturn(projectsFile);
	}

	@Test
	void testSettingsUpdateHandler() throws Exception
	{
		final SettingsUpdateRequest request = new SettingsUpdateRequest(SettingsDto.builder()
				.autoLoadLatestProjectOnStart(true)
				.unsavedChangesMode(UnsavedChangesMode.DISCARD)
				.build());

		handler.handleRequest(request);

		assertThat(applicationEvents.stream(SettingsUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(e -> assertThat(e.getSettings()).isEqualTo(SettingsDto.builder()
						.autoLoadLatestProjectOnStart(true)
						.unsavedChangesMode(UnsavedChangesMode.DISCARD)
						.build()));

	}

	@Test
	@SuppressWarnings({"unchecked", "rawtypes", "java:S1871"})
	void testUndoOperation() throws Exception
	{
		final SettingsUpdateRequest request = new SettingsUpdateRequest(SettingsDto.builder()
				.autoLoadLatestProjectOnStart(true)
				.unsavedChangesMode(UnsavedChangesMode.DISCARD)
				.build());

		requestExecutor.execute(request);

		assertThat(applicationEvents.stream(SettingsUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(e -> assertThat(e.getSettings()).isEqualTo(SettingsDto.builder()
						.autoLoadLatestProjectOnStart(true)
						.unsavedChangesMode(UnsavedChangesMode.DISCARD)
						.build()));

		applicationEvents.clear();

		final OneTimeActionRequestHandler undoHandler = (OneTimeActionRequestHandler) requestHandlerFactory.getRequestHandler(UndoRequest.class).orElseThrow();
		undoHandler.handleRequest(new UndoRequest());

		assertThat(applicationEvents.stream(SettingsUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(e -> assertThat(e.getSettings()).isEqualTo(SettingsDto.builder()
						.autoLoadLatestProjectOnStart(false)
						.unsavedChangesMode(UnsavedChangesMode.ASK)
						.build()));
	}
}
