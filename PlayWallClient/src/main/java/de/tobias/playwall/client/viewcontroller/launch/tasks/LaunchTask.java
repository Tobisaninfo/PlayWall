package de.tobias.playwall.client.viewcontroller.launch.tasks;

import javafx.scene.control.Label;
import lombok.AllArgsConstructor;
import lombok.Getter;

public abstract class LaunchTask
{
	@SuppressWarnings("java:S2094")
	public abstract static sealed class LaunchResult
	{
	}

	public static final class SuccessResult extends LaunchResult
	{
	}

	@AllArgsConstructor
	@Getter
	public static final class FailureResult extends LaunchResult
	{
		private final String errorMessage;
		private final Throwable throwable;
		private final boolean showLogFolderButton;
	}

	public abstract LaunchResult launch(Label progressLabel);
}
