package de.tobias.playwall.server.common.model.project;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
	private List<UUID> allProjectsMetadata = new ArrayList<>();
}
