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
	@SuppressWarnings({"java:S116", "java:S1170"})
	private final int VERSION = 1;

	@Builder.Default
	private RecentProjectsStack recentProjects = new RecentProjectsStack();

	@Builder.Default
	private List<UUID> allProjects = new ArrayList<>();
}
