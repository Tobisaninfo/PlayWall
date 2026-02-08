package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
class ProjectDumpController
{
	private final ProjectController projectController;

	@GetMapping("/debug/current-project")
	Project dumpCurrentProject()
	{
		return projectController.getLoadedProject();
	}
}
