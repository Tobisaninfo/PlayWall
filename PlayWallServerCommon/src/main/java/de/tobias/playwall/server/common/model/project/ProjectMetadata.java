package de.tobias.playwall.server.common.model.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.common.TimeMode;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@EqualsAndHashCode
public class ProjectMetadata
{
	private static final double DEFAULT_VOLUME = 1.0;

	@SuppressWarnings({"java:S116", "java:S1170"})
	private final int VERSION = 1;

	@JsonView(Views.AllProjectsInfo.class)
	private UUID id;

	@JsonView(Views.AllProjectsInfo.class)
	private String name;

	private Integer numberOfHorizontalPads;
	private Integer numberOfVerticalPads;

	@Builder.Default
	private Double volume = DEFAULT_VOLUME;

	@Builder.Default
	private TimeMode timeMode = TimeMode.ELAPSED;

	@Builder.Default
	private Color defaultColor = Color.GRAY1;

	@Builder.Default
	private Color playColor = Color.RED3;

	@Builder.Default
	private Color introColor = Color.BLUE1;

	@Builder.Default
	private Double eofWarningTime = 5.0;

	@Builder.Default
	private FadeSettings fadeSettings = new FadeSettings();

	@JsonIgnore
	public int getNumberOfPadsPerPage()
	{
		return numberOfHorizontalPads * numberOfVerticalPads;
	}

	public ProjectMetadata copy(boolean generateNewId)
	{
		return ProjectMetadata.builder()
				.id(generateNewId ? UUID.randomUUID() : id)
				.name(name)
				.numberOfHorizontalPads(numberOfHorizontalPads)
				.numberOfVerticalPads(numberOfVerticalPads)
				.volume(volume)
				.timeMode(timeMode)
				.defaultColor(defaultColor)
				.playColor(playColor)
				.introColor(introColor)
				.eofWarningTime(eofWarningTime)
				.fadeSettings(fadeSettings)
				.build();
	}
}
