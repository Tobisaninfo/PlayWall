package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.page.request.PageImportRequest;
import de.tobias.playwall.common.api.page.update.PageAddUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.page.model.PageExport;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.util.MimeTypeUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class PageImportHandlerTest extends AbstractUndoableRequestHandlerTest<PageImportRequest>
{
	@MockitoBean
	private PathProvider pathProvider;

	@Autowired
	private PageImportHandler handler;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private ApplicationEvents applicationEvents;

	@Test
	void testImportPage() throws Exception
	{
		final Project project = TestUtils.loadProject(jsonMapper, "projects/project_2.json");
		projectController.loadProject(project).get();

		final byte[] importPageFile = new ClassPathResource("projects/page_1.json").getInputStream().readAllBytes();
		final PageExport pageExport = jsonMapper.readValue(importPageFile, PageExport.class);

		final String base64 = Base64.getEncoder().encodeToString(importPageFile);
		final Optional<UndoItem> response = handler.handleRequest(new PageImportRequest(MimeTypeUtils.APPLICATION_JSON_VALUE, base64));
		assertThat(response).isNotEmpty();

		assertThat(applicationEvents.stream(PageAddUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(update -> {
					assertThat(update.getPage().id()).isNotEqualTo(pageExport.page().getId());
					assertThat(update.getPage().settings().name()).isEqualTo(pageExport.page().getSettings().getName());
					assertThat(update.getPage().settings().color()).isEqualTo(Color.GRAY1);
					assertThat(update.getPage().position()).isZero();

					assertThat(update.getPage().pads()).
							usingRecursiveComparison()
							.ignoringFieldsMatchingRegexes("id")
							.isEqualTo(pageExport.page().getPads());
					assertThat(update.getPage().pads()).
							usingRecursiveComparison()
							.comparingOnlyFields("id")
							.isNotEqualTo(pageExport.page().getPads());

					assertThat(projectController.getLoadedProject().getPageById(update.getPage().id())).isPresent();
				});
	}

	@Test
	void testImportPageNameCollision() throws Exception
	{
		final Project project = TestUtils.loadProject(jsonMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final byte[] importPageFile = new ClassPathResource("projects/page_1.json").getInputStream().readAllBytes();
		final PageExport pageExport = jsonMapper.readValue(importPageFile, PageExport.class);

		final String base64 = Base64.getEncoder().encodeToString(importPageFile);
		final Optional<UndoItem> response = handler.handleRequest(new PageImportRequest(MimeTypeUtils.APPLICATION_JSON_VALUE, base64));
		assertThat(response).isNotEmpty();

		assertThat(applicationEvents.stream(PageAddUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(update -> {
					assertThat(update.getPage().settings().name()).isEqualTo(pageExport.page().getSettings().getName() + " - 1");
					assertThat(update.getPage().position()).isEqualTo(1);
				});
	}

	@ParameterizedTest
	@ValueSource(strings = {"projects/page_no_header.json", "projects/page_too_old.json", "projects/page_unsupported.json", "projects/page_wrong_mimetype.json"})
	void testImportPageNoHeader(String inputFile) throws Exception
	{
		final Project project = TestUtils.loadProject(jsonMapper, "projects/project_2.json");
		projectController.loadProject(project).get();

		final byte[] importPageFile = new ClassPathResource(inputFile).getInputStream().readAllBytes();

		final String base64 = Base64.getEncoder().encodeToString(importPageFile);
		final PageImportRequest requestMessage = new PageImportRequest(MimeTypeUtils.APPLICATION_JSON_VALUE, base64);

		assertThatThrownBy(() -> handler.handleRequest(requestMessage))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testUndoOperation() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");

		final byte[] importPageFile = new ClassPathResource("projects/page_1.json").getInputStream().readAllBytes();
		final String base64 = Base64.getEncoder().encodeToString(importPageFile);
		final PageImportRequest request = new PageImportRequest(MimeTypeUtils.APPLICATION_JSON_VALUE, base64);

		testInverseOperation(project, request);
	}
}
