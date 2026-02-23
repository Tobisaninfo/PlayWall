package de.tobias.playwall.client.domain.project;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public final class AllProjectsInfo
{
	private List<UUID> recentProjectIds;
	private List<ProjectMetadata> allProjectsMetadata;
}
