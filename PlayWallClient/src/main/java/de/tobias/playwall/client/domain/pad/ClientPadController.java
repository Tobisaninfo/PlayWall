package de.tobias.playwall.client.domain.pad;

import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.pad.update.PadWarningAnimationPlayUpdate;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class ClientPadController
{
	private final UpdateMessageEventHandler eventHandler;

	private Pad pad;

	private PadStatus status;
	private Duration duration;
	private Duration position;
	private ProjectMetadata	projectMetadata;

	private final BooleanProperty shouldWarningAnimationPlayEndOfFile = new SimpleBooleanProperty(false);
	private final BooleanProperty shouldWarningAnimationPlayFading = new SimpleBooleanProperty(false);
	private final BooleanBinding shouldWarningAnimationPlay = Bindings.or(shouldWarningAnimationPlayEndOfFile, shouldWarningAnimationPlayFading);

	public ClientPadController(UpdateMessageEventHandler eventHandler, Pad pad, ProjectMetadata projectMetadata)
	{
		this.eventHandler = eventHandler;
		this.pad = pad;
		this.projectMetadata = projectMetadata;

		shouldWarningAnimationPlay.addListener((_, _, newValue) ->
				eventHandler.fireEvent(new PadWarningAnimationPlayUpdate(pad.getId(), newValue)));
	}

	public boolean shouldWarningAnimationPlay()
	{
		return shouldWarningAnimationPlay.get();
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
