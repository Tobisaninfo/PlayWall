package de.tobias.playwall.client.domain.pad;

import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class ClientPadController
{
	@Setter
	private Pad pad;

	private PadStatus status;
	private Duration duration;
	private Duration position;
	private ProjectMetadata	projectMetadata;

	public ClientPadController(Pad pad, ProjectMetadata projectMetadata)
	{
		this.pad = pad;
		this.projectMetadata = projectMetadata;
	}

	public boolean isWarningThresholdReached()
	{
		final Duration remaining = getRemainingTime();
		final double warningThreshold = getEffectiveEofWarningTime();

		return remaining.toSeconds() < warningThreshold;
	}

	private Duration getRemainingTime()
	{
		return duration.subtract(position);
	}

	public double getEffectiveEofWarningTime()
	{
		return Optional.ofNullable(pad.getEofWarningTime())
				.orElse(projectMetadata.getEofWarningTime());
	}

	public ModernColor getEffectiveDefaultColor()
	{
		return Optional.ofNullable(pad.getDefaultColor())
				.orElse(projectMetadata.getDefaultColor());
	}

	public ModernColor getEffectivePlayColor()
	{
		return Optional.ofNullable(pad.getPlayColor())
				.orElse(projectMetadata.getPlayColor());
	}
}
