package de.tobias.playwall.server.common.model.project;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@EqualsAndHashCode
public class AllProjectsInfo
{
	@Builder.Default
	@JsonView(Views.AllProjectsInfo.class)
	private RecentProjectsStack recentProjects = new RecentProjectsStack();

	@Builder.Default
	@JsonView(Views.AllProjectsInfo.class)
	private List<ProjectMetadata> allProjectsMetadata = new ArrayList<>();
}
