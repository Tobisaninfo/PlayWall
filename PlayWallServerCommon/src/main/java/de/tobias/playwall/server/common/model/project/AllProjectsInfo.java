package de.tobias.playwall.server.common.model.project;

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
	private RecentProjectsStack recentProjects = new RecentProjectsStack();

	@Builder.Default
	private List<ProjectMetadata> allProjectsMetadata = new ArrayList<>();
}
