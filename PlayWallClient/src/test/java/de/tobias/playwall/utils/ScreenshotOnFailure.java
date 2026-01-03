package de.tobias.playwall.utils;

import javafx.stage.Stage;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.testfx.api.FxToolkit;

public class ScreenshotOnFailure implements TestWatcher
{
	@Override
	public void testFailed(ExtensionContext context, Throwable cause)
	{
		final String testName = context.getDisplayName();
		final Stage stage = FxToolkit.toolkitContext().getRegisteredStage();
		ScreenshotUtil.takeScreenshot(stage.getScene().getRoot(), "screenshots/" + testName + ".png");
	}
}