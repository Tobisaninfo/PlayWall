package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectPageShowRequest;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.api.project.PageIndexOutOfRangeException;
import de.tobias.playwall.server.api.project.ProjectNotLoadedException;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectPageShowHandlerTest extends AbstractRequestHandlerTest
{
	@Autowired
	private JsonMapper objectMapper;

	@Autowired
	private ProjectController projectController;

	@Autowired
	private ProjectPageShowHandler handler;

	@BeforeEach
	void init()
	{
		projectController.unloadProject();
	}

	@Test
	void testProjectPageShowRequestUpdatesCurrentPageIndex() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		projectController.loadProject(project).get();

		handler.handleRequest(new ProjectPageShowRequest(1));

		assertThat(projectController.getCurrentPageIndex()).isEqualTo(1);
	}

	@Test
	void testProjectPageShowRequestWithoutLoadedProject()
	{
		final ProjectPageShowRequest request = new ProjectPageShowRequest(1);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(ProjectNotLoadedException.class);
	}

	@Test
	void testProjectPageShowRequestWithIndexBeyondPageCount() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final ProjectPageShowRequest request = new ProjectPageShowRequest(1);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(PageIndexOutOfRangeException.class);

		assertThat(projectController.getCurrentPageIndex()).isZero();
	}

	@Test
	void testProjectPageShowRequestWithNegativeIndex() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		projectController.loadProject(project).get();

		final ProjectPageShowRequest request = new ProjectPageShowRequest(-1);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(PageIndexOutOfRangeException.class);
	}
}
