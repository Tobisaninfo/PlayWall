package de.tobias.playwall.client.view.components;

import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ViewConstants
{
	public static final double DEFAULT_SPACING = 14.0;
	public static final Duration DEFAULT_SNACKBAR_SHOW = Duration.seconds(3);

	public static final String PAGE_BUTTON_CURRENT_STYLECLASS = "current-page-button";
}
