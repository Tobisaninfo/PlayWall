package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectExportRequest;
import de.tobias.playwall.common.api.project.request.ProjectExportResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.common.storage.PathProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectExportHandlerTest extends AbstractRequestHandlerTest
{
	private final UUID projectId = UUID.randomUUID();

	@TempDir
	private Path tempDir;

	@MockitoBean
	private PathProvider pathProvider;

	@Autowired
	private ProjectExportHandler handler;

	private byte[] projectFile;

	@BeforeEach
	void init() throws IOException
	{
		final Path projectFilePath = tempDir.resolve(projectId + ".json");
		projectFile = new ClassPathResource("projects/project_1.json").getInputStream().readAllBytes();
		Files.write(projectFilePath, projectFile);
		when(pathProvider.getPathForProject(any())).thenReturn(projectFilePath);
	}

	@Test
	void testExportProject() throws IOException
	{
		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectExportRequest(projectId));
		assertThat(response).isNotEmpty();

		final ProjectExportResponse exportResponse = response.map(ProjectExportResponse.class::cast).get();
		final byte[] bytes = Base64.getDecoder().decode(exportResponse.getBase64());
		assertThat(bytes).isEqualTo(projectFile);
		assertThat(exportResponse.getMimetype()).isEqualTo("application/json");
	}
}
