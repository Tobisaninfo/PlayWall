package de.tobias.playwall.client.domain.project.view.media;

import lombok.*;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(of = {"padId"})
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public final class MissingMediaEntry
{
	private final String pageName;

	private final UUID padId;

	private final int padPosition;

	private final String padName;

	private final String oldMediaPath;

	@Setter
	@Builder.Default
	private String newMediaPath = null;

	@Setter
	@Builder.Default
	private MissingMediaSolutionType missingMediaSolutionType = MissingMediaSolutionType.NONE;
}
