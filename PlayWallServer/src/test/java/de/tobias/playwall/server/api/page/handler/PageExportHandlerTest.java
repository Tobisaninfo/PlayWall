package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageExportRequest;
import de.tobias.playwall.common.api.page.request.PageExportResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PageExportHandlerTest
{
	@MockitoBean
	private PathProvider pathProvider;

	@Autowired
	private PageExportHandler handler;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private ProjectController projectController;

	private byte[] expectedBytes;


	@BeforeEach
	void init() throws Exception
	{
		expectedBytes = new ClassPathResource("projects/page_1.json").getInputStream().readAllBytes();

		final Project project = TestUtils.loadProject(jsonMapper, "projects/project_1.json");
		projectController.loadProject(project).get();
	}

	@Test
	void testExportProject() throws IOException
	{
		final UUID pageId = UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea");

		final Optional<ResponseMessage> response = handler.handleRequest(new PageExportRequest(pageId));
		assertThat(response).isNotEmpty();

		final PageExportResponse exportResponse = response.map(PageExportResponse.class::cast).get();
		final byte[] bytes = Base64.getDecoder().decode(exportResponse.getBase64());

		final String actual = jsonMapper.writeValueAsString(jsonMapper.readTree(bytes));
		final String expected = jsonMapper.writeValueAsString(jsonMapper.readTree(expectedBytes));

		assertThat(actual).isEqualTo(expected);

		assertThat(exportResponse.getMimetype()).isEqualTo("application/json");
	}
}
